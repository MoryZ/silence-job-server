package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.HashUtil;
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
import com.old.silence.job.server.common.config.SystemProperties;
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

import java.util.Objects;

/**
 * OPENAPI
 * 新增定时任务
 */
@Component
public class OpenApiAddJobRequestHandler extends PostHttpRequestHandler {
    private final SystemProperties systemProperties;
    private final JobDao jobDao;

    public OpenApiAddJobRequestHandler(SystemProperties systemProperties, JobDao jobDao) {
        this.systemProperties = systemProperties;
        this.jobDao = jobDao;
    }

    private static Long calculateNextTriggerAt(JobRequestVO jobRequestVO, Long time) {
        if (Objects.equals(jobRequestVO.getTriggerType(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
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
        return HTTP_PATH.OPENAPI_ADD_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Add job content:[{}]", content);
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobRequestVO jobRequestVO = JSON.parseObject(JSON.toJSONString(args[0]), JobRequestVO.class);
        if (StrUtil.isBlank(jobRequestVO.getGroupName())) {
            jobRequestVO.setGroupName(HttpHeaderUtil.getGroupName(headers));
        }
        // 判断常驻任务
        Job job = JobConverter.INSTANCE.convert(jobRequestVO);
        job.setResident(isResident(jobRequestVO));
        job.setBucketIndex(HashUtil.bkdrHash(jobRequestVO.getGroupName() + jobRequestVO.getJobName())
                % systemProperties.getBucketTotal());
        job.setNextTriggerAt(calculateNextTriggerAt(jobRequestVO, DateUtils.toNowMilli()));
        job.setNamespaceId(HttpHeaderUtil.getNamespace(headers));
        job.setId(null);
        Assert.isTrue(1 == jobDao.insert(job), () -> new SilenceJobServerException("新增任务失败"));
        return new SilenceJobRpcResult(job.getId(), retryRequest.getReqId());
    }

    private boolean isResident(JobRequestVO jobRequestVO) {
        if (Objects.equals(jobRequestVO.getTriggerType().getValue().intValue(), SystemConstants.WORKFLOW_TRIGGER_TYPE)) {
            return false;
        }

        if (jobRequestVO.getTriggerType().getValue() == WaitStrategies.WaitStrategyEnum.FIXED.getValue()) {
            return Integer.parseInt(jobRequestVO.getTriggerInterval()) < 10;
        } else if (jobRequestVO.getTriggerType().getValue() == WaitStrategies.WaitStrategyEnum.CRON.getValue()) {
            return CronUtils.getExecuteInterval(jobRequestVO.getTriggerInterval()) < 10 * 1000;
        } else {
            throw new SilenceJobServerException("未知触发类型");
        }
    }
}
