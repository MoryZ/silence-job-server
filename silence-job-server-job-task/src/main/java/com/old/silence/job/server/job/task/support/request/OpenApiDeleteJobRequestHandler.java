package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.constant.SystemConstants.HTTP_PATH;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.common.util.HttpHeaderUtil;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobSummary;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobSummaryDao;

import java.util.List;
import java.util.Set;


/**
 * OPENAPI
 * 删除定时任务
 */
@Component
public class OpenApiDeleteJobRequestHandler extends PostHttpRequestHandler {
    private final JobDao jobDao;
    private final JobSummaryDao jobSummaryDao;

    public OpenApiDeleteJobRequestHandler(JobDao jobDao, JobSummaryDao jobSummaryDao) {
        this.jobDao = jobDao;
        this.jobSummaryDao = jobSummaryDao;
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_DELETE_JOB.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.debug("Delete job content:[{}]", content);
        SilenceJobRequest request = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = request.getArgs();
        Set<Long> ids = JSON.parseObject(JSON.toJSONString(args[0]), Set.class);
        String namespaceId = HttpHeaderUtil.getNamespace(headers);

        Assert.isTrue(ids.size() == jobDao.delete(
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getNamespaceId, namespaceId)
                        .eq(Job::getJobStatus, 500)
                        .in(Job::getId, ids)
        ), () -> new SilenceJobServerException("删除定时任务失败, 请检查任务状态是否关闭状态"));

        List<JobSummary> jobSummaries = jobSummaryDao.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .eq(JobSummary::getSystemTaskType, SystemTaskType.JOB)
        );
        if (CollectionUtils.isNotEmpty(jobSummaries)) {
            jobSummaryDao.deleteBatchIds(StreamUtils.toSet(jobSummaries, JobSummary::getId));
        }

        return new SilenceJobRpcResult(true, request.getReqId());
    }
}
