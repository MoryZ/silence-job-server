package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.common.vo.JobStatusUpdateRequestVO;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;

/**
 * OPENAPI
 * 更新定时任务状态
 */
@Component
public class OpenApiUpdateJobStatusRequestHandler extends PostHttpRequestHandler {
    private final JobDao jobDao;

    public OpenApiUpdateJobStatusRequestHandler(JobDao jobDao) {
        this.jobDao = jobDao;
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_UPDATE_JOB_STATUS.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();
        JobStatusUpdateRequestVO jobRequestVO = JSON.parseObject(JSON.toJSONString(args[0]), JobStatusUpdateRequestVO.class);
        Long count = jobDao.selectCount(new LambdaQueryWrapper<Job>().eq(Job::getId, jobRequestVO.getId()));
        if (1 != count) {
            SilenceJobLog.LOCAL.warn("更新任务失败");
            return new SilenceJobRpcResult(false, retryRequest.getReqId());
        }
        Job job = new Job();
        job.setId(jobRequestVO.getId());
        job.setJobStatus(jobRequestVO.getJobStatus());
        boolean update = 1 == jobDao.updateById(job);
        return new SilenceJobRpcResult(update, retryRequest.getReqId());

    }
}
