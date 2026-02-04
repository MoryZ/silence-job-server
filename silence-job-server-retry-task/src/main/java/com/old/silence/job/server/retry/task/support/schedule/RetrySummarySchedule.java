package com.old.silence.job.server.retry.task.support.schedule;

import org.springframework.stereotype.Component;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Sets;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.Lifecycle;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.schedule.AbstractSchedule;
import com.old.silence.job.server.common.triple.Triple;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySummary;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySummaryDao;
import com.old.silence.job.server.vo.DashboardRetryResponseDO;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Retry Dashboard
 *
 */
@Component
public class RetrySummarySchedule extends AbstractSchedule implements Lifecycle {
    private final RetryDao retryDao;
    private final RetrySummaryDao retrySummaryDao;
    private final SystemProperties systemProperties;

    public RetrySummarySchedule(RetryDao retryDao, RetrySummaryDao retrySummaryDao,
                                SystemProperties systemProperties) {
        this.retryDao = retryDao;
        this.retrySummaryDao = retrySummaryDao;
        this.systemProperties = systemProperties;
    }

    @Override
    public String lockName() {
        return "retrySummaryDashboard";
    }

    @Override
    public String lockAtMost() {
        return "PT1M";
    }

    @Override
    public String lockAtLeast() {
        return "PT20S";
    }

    @Override
    protected void doExecute() {
        try {
            var now = Instant.now();
            ZoneId zoneId = ZoneId.systemDefault();
            for (int i = 0; i < systemProperties.getSummaryDay(); i++) {


                // 重试按日实时查询统计数据（00:00:00 - 23:59:59）
                Instant todayFrom = now.atZone(zoneId).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant()
                        .plus(-i, ChronoUnit.DAYS);
                Instant todayTo = now.atZone(zoneId).withHour(23).withMinute(59).withSecond(59).withNano(999999999).toInstant()
                        .plus(-i, ChronoUnit.DAYS);
                LambdaQueryWrapper<Retry> wrapper = new LambdaQueryWrapper<Retry>()
                        .between(Retry::getCreatedDate, todayFrom, todayTo)
                        .groupBy(Retry::getNamespaceId, Retry::getGroupName, Retry::getSceneName);
                List<DashboardRetryResponseDO> dashboardRetryResponseDOList = retryDao.selectRetrySummaryList(wrapper);
                if (CollectionUtils.isEmpty(dashboardRetryResponseDOList)) {
                    continue;
                }

                // insertOrUpdate
                List<RetrySummary> retrySummaryList = retrySummaryList(todayFrom, dashboardRetryResponseDOList);

                Set<String> groupNames = Sets.newHashSet();
                Set<String> namespaceIds = Sets.newHashSet();
                Set<String> sceneNames = Sets.newHashSet();
                for (final RetrySummary retrySummary : retrySummaryList) {
                    groupNames.add(retrySummary.getGroupName());
                    namespaceIds.add(retrySummary.getNamespaceId());
                    sceneNames.add(retrySummary.getSceneName());
                }

                List<RetrySummary> retrySummaries = retrySummaryDao.selectList(new LambdaQueryWrapper<RetrySummary>()
                        .in(RetrySummary::getGroupName, groupNames)
                        .in(RetrySummary::getNamespaceId, namespaceIds)
                        .in(RetrySummary::getSceneName, sceneNames)
                        .eq(RetrySummary::getTriggerAt, todayFrom)
                );

                Map<Triple<String, String, Instant>, RetrySummary> summaryMap = StreamUtils.toIdentityMap(
                        retrySummaries,
                        retrySummary -> Triple.of(mergeKey(retrySummary), retrySummary.getSceneName(), retrySummary.getTriggerAt()));

                List<RetrySummary> waitInserts = new ArrayList<>();
                List<RetrySummary> waitUpdates = new ArrayList<>();
                for (final RetrySummary retrySummary : retrySummaryList) {
                    if (Objects.isNull(summaryMap.get(Triple.of(mergeKey(retrySummary), retrySummary.getSceneName(), retrySummary.getTriggerAt())))) {
                        waitInserts.add(retrySummary);
                    } else {
                        waitUpdates.add(retrySummary);
                    }
                }

                int insertTotalRetrySummary = 0;
                if (CollectionUtils.isNotEmpty(waitInserts)) {
                    insertTotalRetrySummary = retrySummaryDao.insertBatch(waitInserts);
                }

                int updateTotalRetrySummary = 0;
                if (CollectionUtils.isNotEmpty(waitUpdates)) {
                    updateTotalRetrySummary = retrySummaryDao.updateBatch(waitUpdates);
                }

                SilenceJobLog.LOCAL.debug("retry summary dashboard success todayFrom:[{}] todayTo:[{}] insertTotalRetrySummary:[{}] updateTotalRetrySummary:[{}]", todayFrom, todayTo, insertTotalRetrySummary, updateTotalRetrySummary);
            }
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("retry summary dashboard log error", e);
        }
    }

    private String mergeKey(final RetrySummary retrySummary) {
        return retrySummary.getGroupName() + retrySummary.getNamespaceId();
    }

    private List<RetrySummary> retrySummaryList(Instant triggerAt, List<DashboardRetryResponseDO> dashboardRetryResponseDOList) {
        List<RetrySummary> retrySummaryList = new ArrayList<>();
        for (DashboardRetryResponseDO dashboardRetryResponseDO : dashboardRetryResponseDOList) {
            RetrySummary retrySummary = new RetrySummary();
            retrySummary.setTriggerAt(triggerAt);
            retrySummary.setNamespaceId(dashboardRetryResponseDO.getNamespaceId());
            retrySummary.setGroupName(dashboardRetryResponseDO.getGroupName());
            retrySummary.setSceneName(dashboardRetryResponseDO.getSceneName());
            retrySummary.setRunningNum(dashboardRetryResponseDO.getRunningNum());
            retrySummary.setFinishNum(dashboardRetryResponseDO.getFinishNum());
            retrySummary.setMaxCountNum(dashboardRetryResponseDO.getMaxCountNum());
            retrySummary.setSuspendNum(dashboardRetryResponseDO.getSuspendNum());
            retrySummaryList.add(retrySummary);
        }
        return retrySummaryList;
    }

    @Override
    public void start() {
        taskScheduler.scheduleAtFixedRate(this::execute, Duration.parse("PT1M"));
    }

    @Override
    public void close() {

    }
}
