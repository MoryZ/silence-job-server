package com.old.silence.job.server.job.task.support.request;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.net.url.UrlQuery;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.client.dto.request.MapTaskRequest;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.JobTaskType;
import com.old.silence.job.common.enums.MapReduceStage;
import com.old.silence.job.common.model.SilenceJobRequest;
import com.old.silence.job.common.model.SilenceJobRpcResult;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.handler.PostHttpRequestHandler;
import com.old.silence.job.server.common.util.HttpHeaderUtil;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTask;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.support.JobExecutor;
import com.old.silence.job.server.job.task.support.JobTaskConverter;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorContext;
import com.old.silence.job.server.job.task.support.executor.job.JobExecutorFactory;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGenerateContext;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGenerator;
import com.old.silence.job.server.job.task.support.generator.task.JobTaskGeneratorFactory;
import com.old.silence.job.server.job.task.support.handler.JobTaskBatchHandler;

import java.util.List;
import java.util.Objects;


/**
 * 动态分片客户端生成map任务
 */
@Component
public class MapTaskPostHttpRequestHandler extends PostHttpRequestHandler {
    private final WorkflowTaskBatchDao workflowTaskBatchDao;
    private final JobDao jobDao;
    private final JobTaskBatchHandler jobTaskBatchHandler;

    public MapTaskPostHttpRequestHandler(WorkflowTaskBatchDao workflowTaskBatchDao, JobDao jobDao,
                                         JobTaskBatchHandler jobTaskBatchHandler) {
        this.workflowTaskBatchDao = workflowTaskBatchDao;
        this.jobDao = jobDao;
        this.jobTaskBatchHandler = jobTaskBatchHandler;
    }

    private static JobExecutorContext buildJobExecutorContext(MapTaskRequest mapTaskRequest, Job job,
                                                              List<JobTask> taskList, String newWfContext) {
        JobExecutorContext context = JobTaskConverter.INSTANCE.toJobExecutorContext(job);
        context.setTaskList(taskList);
        context.setTaskBatchId(mapTaskRequest.getTaskBatchId());
        context.setWorkflowTaskBatchId(mapTaskRequest.getWorkflowTaskBatchId());
        context.setWorkflowNodeId(mapTaskRequest.getWorkflowNodeId());
        context.setWfContext(newWfContext);
        return context;
    }

    @Override
    public boolean supports(String path) {
        return SystemConstants.HTTP_PATH.BATCH_REPORT_JOB_MAP_TASK.equals(path);
    }

    @Override
    public HttpMethod method() {
        return HttpMethod.POST;
    }

    @Override
    public SilenceJobRpcResult doHandler(String content, UrlQuery query, HttpHeaders headers) {
        SilenceJobLog.LOCAL.info("map task Request. content:[{}]", content);
        String groupName = HttpHeaderUtil.getGroupName(headers);
        String namespace = HttpHeaderUtil.getNamespace(headers);

        SilenceJobRequest retryRequest = JSON.parseObject(content, SilenceJobRequest.class);
        Object[] args = retryRequest.getArgs();
        MapTaskRequest mapTaskRequest = JSON.parseObject(JSON.toJSONString(args[0]), MapTaskRequest.class);

        Job job = jobDao.selectOne(new LambdaQueryWrapper<Job>()
                .eq(Job::getId, mapTaskRequest.getJobId())
                .eq(Job::getGroupName, groupName)
                .eq(Job::getNamespaceId, namespace)
        );

        if (Objects.isNull(job)) {
            return new SilenceJobRpcResult(500, "Job config not existed", Boolean.FALSE,
                    retryRequest.getReqId());
        }

        String argStr = jobTaskBatchHandler.getArgStr(mapTaskRequest.getTaskBatchId(), job);

        // 创建map任务
        JobTaskGenerator taskInstance = JobTaskGeneratorFactory.getTaskInstance(job.getTaskType());
        JobTaskGenerateContext context = JobTaskConverter.INSTANCE.toJobTaskInstanceGenerateContext(mapTaskRequest);
        context.setGroupName(HttpHeaderUtil.getGroupName(headers));
        context.setArgsStr(argStr);
        context.setNamespaceId(HttpHeaderUtil.getNamespace(headers));
        context.setMrStage(MapReduceStage.MAP);
        context.setMapSubTask(mapTaskRequest.getSubTask());
        context.setWfContext(mapTaskRequest.getWfContext());
        List<JobTask> taskList = taskInstance.generate(context);
        if (CollectionUtils.isEmpty(taskList)) {
            return new SilenceJobRpcResult(500, "Job task is empty", Boolean.FALSE,
                    retryRequest.getReqId());
        }

        String newWfContext = null;
        if (Objects.nonNull(mapTaskRequest.getWorkflowTaskBatchId()) && mapTaskRequest.getWorkflowTaskBatchId().longValue() > 0) {
            WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectOne(
                    new LambdaQueryWrapper<WorkflowTaskBatch>()
                            .select(WorkflowTaskBatch::getWfContext, WorkflowTaskBatch::getId)
                            .eq(WorkflowTaskBatch::getId, mapTaskRequest.getWorkflowTaskBatchId())
            );
            Assert.notNull(workflowTaskBatch, () -> new SilenceJobServerException("workflowTaskBatch is null. id:[{}]", mapTaskRequest.getWorkflowTaskBatchId()));
            newWfContext = workflowTaskBatch.getWfContext();
        }

        // 执行任务
        JobExecutor jobExecutor = JobExecutorFactory.getJobExecutor(JobTaskType.MAP_REDUCE);
        jobExecutor.execute(buildJobExecutorContext(mapTaskRequest, job, taskList, newWfContext));

        return new SilenceJobRpcResult(200, "Report Map Task Processed Successfully", Boolean.TRUE,
                retryRequest.getReqId());
    }

}
