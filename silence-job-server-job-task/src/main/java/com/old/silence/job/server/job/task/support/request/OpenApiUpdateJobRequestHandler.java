package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.convert.JobConverter;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.util.CronUtils;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.common.util.HttpHeaderUtil;
import com.old.silence.job.server.common.vo.JobRequestVO;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.job.task.support.cache.ResidentTaskCache;

import java.util.Objects;
import java.util.Optional;

/**
 * OPENAPI
 * 更新定时任务
 */
@Component
public class OpenApiUpdateJobRequestHandler extends PostHttpRequestHandler {
    private final JobDao jobDao;

    public OpenApiUpdateJobRequestHandler(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    private static Long calculateNextTriggerAt(JobRequestVO jobRequestVO, Long time) {
        if (Objects.equals(jobRequestVO.getTriggerType().getValue().intValue(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return 0L;
        }

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(jobRequestVO.getTriggerType().getValue());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(jobRequestVO.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_UPDATE_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Update job content:[{}]", content);
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();
        String namespace = HttpHeaderUtil.getNamespace(headers);
        JobRequestVO jobRequestVO = JSON.parseObject(JSON.toJSONString(args[0]), JobRequestVO.class);
        if (Objects.isNull(jobRequestVO.getId())) {
            SilenceJobLog.LOCAL.warn("id不能为空，更新失败");
            return new SilenceJobRpcResult(false, retryRequest.getReqId());
        }

        Job job = jobDao.selectById(jobRequestVO.getId());
        if (Objects.isNull(job)) {
            SilenceJobLog.LOCAL.warn("job为空，更新失败");
            return new SilenceJobRpcResult(false, retryRequest.getReqId());
        }

        // 判断常驻任务
        Job updateJob = JobConverter.INSTANCE.convert(jobRequestVO);
        updateJob.setResident(isResident(jobRequestVO));
        updateJob.setNamespaceId(namespace);

        // 工作流任务
        if (Objects.equals(jobRequestVO.getTriggerType().getValue().intValue(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            job.setNextTriggerAt(0L);
            // 非常驻任务 > 非常驻任务
        } else if (!job.getResident() && !updateJob.getResident()) {
            updateJob.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, DateUtils.toNowMilli()));
        } else if (job.getResident() && !updateJob.getResident()) {
            // 常驻任务的触发时间
            long time = Optional.ofNullable(ResidentTaskCache.get(jobRequestVO.getId()))
                    .orElse(DateUtils.toNowMilli());
            updateJob.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, time));
            // 老的是不是常驻任务 新的是常驻任务 需要使用当前时间计算下次触发时间
        } else if (!job.getResident()) {
            updateJob.setNextTriggerAt(DateUtils.toNowMilli());
        }

        // 禁止更新组
        updateJob.setGroupName(null);
        boolean insert = 1 == jobDao.updateById(updateJob);
        return new SilenceJobRpcResult(insert, retryRequest.getReqId());

    }

    private boolean isResident(JobRequestVO jobRequestVO) {
        if (Objects.equals(jobRequestVO.getTriggerType().getValue().intValue(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return false;
        }

        if (jobRequestVO.getTriggerType().getValue().intValue() == WaitStrategies.WaitStrategyEnum.FIXED.getValue()) {
            return Integer.parseInt(jobRequestVO.getTriggerInterval()) < 10;
        } else if (jobRequestVO.getTriggerType().getValue().intValue() == WaitStrategies.WaitStrategyEnum.CRON.getValue()) {
            return CronUtils.getExecuteInterval(jobRequestVO.getTriggerInterval()) < 10 * 1000;
        } else {
            throw new SilenceJobServerException("未知触发类型");
        }
    }
}
