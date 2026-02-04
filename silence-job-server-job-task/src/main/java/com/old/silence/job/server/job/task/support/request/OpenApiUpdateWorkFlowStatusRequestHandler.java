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
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowDao;

import java.util.Objects;

/**
 * OPENAPI
 * 更新工作流状态
 */
@Component
public class OpenApiUpdateWorkFlowStatusRequestHandler extends PostHttpRequestHandler {
    private final WorkflowDao workflowDao;

    public OpenApiUpdateWorkFlowStatusRequestHandler(WorkflowDao workflowDao) {
        this.workflowDao = workflowDao;
    }

    @Override
    public boolean supports(String path) {
        return HTTP_PATH.OPENAPI_UPDATE_WORKFLOW_STATUS.equals(path);
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
        Workflow workflow = workflowDao.selectOne(
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId)
                        .eq(Workflow::getId, jobRequestVO.getId()));

        if (Objects.isNull(workflow)) {
            SilenceJobLog.LOCAL.warn("工作流不存在");
            return new SilenceJobRpcResult(false, retryRequest.getReqId());
        }
        workflow.setWorkflowStatus(jobRequestVO.getJobStatus());
        boolean update = 1 == workflowDao.updateById(workflow);

        return new SilenceJobRpcResult(update, retryRequest.getReqId());

    }
}
