package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.convert.JobResponseVOConverter;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.common.vo.JobResponseVO;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;

/**
 * OPENAPI
 * 获取定时任务详情
 */
@Component
public class OpenApiGetJobDetailRequestHandler extends PostHttpRequestHandler {
    private final JobDao jobDao;

    public OpenApiGetJobDetailRequestHandler(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_GET_JOB_DETAIL.equals(path);
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
        Long jobId = JSON.parseObject(JSON.toJSONString(args[0]), Long.class);
        Assert.notNull(jobId, () -> new SilenceJobServerException("id 不能为空"));

        Job job = jobDao.selectById(jobId);
        JobResponseVO convert = JobResponseVOConverter.INSTANCE.convert(job);
        return new SilenceJobRpcResult(convert, retryRequest.getReqId());

    }
}
