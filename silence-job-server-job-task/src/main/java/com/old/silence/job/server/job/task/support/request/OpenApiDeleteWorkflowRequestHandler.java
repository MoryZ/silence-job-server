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
import com.old.silence.job.server.domain.model.JobSummary;
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobSummaryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowDao;

import java.util.List;
import java.util.Set;


/**
 * OPENAPI
 * 删除工作流
 */
@Component
public class OpenApiDeleteWorkflowRequestHandler extends PostHttpRequestHandler {
    private final WorkflowDao workflowDao;
    private final JobSummaryDao jobSummaryDao;

    public OpenApiDeleteWorkflowRequestHandler(WorkflowDao workflowDao, JobSummaryDao jobSummaryDao) {
        this.workflowDao = workflowDao;
        this.jobSummaryDao = jobSummaryDao;
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_DELETE_WORKFLOW.equals(path);
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

        Assert.isTrue(ids.size() == workflowDao.delete(
                new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getNamespaceId, namespaceId)
                        .eq(Workflow::getWorkflowStatus, 500)
                        .in(Workflow::getId, ids)
        ), () -> new SilenceJobServerException("删除工作流任务失败, 请检查任务状态是否关闭状态"));

        List<JobSummary> jobSummaries = jobSummaryDao.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getNamespaceId, namespaceId)
                .eq(JobSummary::getSystemTaskType, SystemTaskType.WORKFLOW)
        );
        if (CollectionUtils.isNotEmpty(jobSummaries)) {
            Assert.isTrue(jobSummaries.size() ==
                            jobSummaryDao.deleteBatchIds(StreamUtils.toSet(jobSummaries, JobSummary::getId)),
                    () -> new SilenceJobServerException("汇总表删除失败")
            );
        }
        return new SilenceJobRpcResult(true, request.getReqId());
    }
}
