package com.old.silence.job.server.domain.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.graph.MutableGraph;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.JobOperationReason;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.api.assembler.JobBatchResponseVOConverter;
import com.old.silence.job.server.api.assembler.JobResponseVOMapper;
import com.old.silence.job.server.api.assembler.WorkflowMapper;
import com.old.silence.job.server.api.config.TenantContext;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.domain.model.WorkflowTaskBatch;
import com.old.silence.job.server.dto.JobTaskConfig;
import com.old.silence.job.server.dto.WorkflowBatchQuery;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.handler.JobHandler;
import com.old.silence.job.server.handler.WorkflowHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowNodeDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowTaskBatchDao;
import com.old.silence.job.server.job.task.support.cache.MutableGraphCache;
import com.old.silence.job.server.job.task.support.handler.WorkflowBatchHandler;
import com.old.silence.job.server.vo.JobBatchResponseVO;
import com.old.silence.job.server.vo.WorkflowBatchResponseDO;
import com.old.silence.job.server.vo.WorkflowBatchResponseVO;
import com.old.silence.job.server.vo.WorkflowDetailResponseVO;
import com.old.silence.core.util.CollectionUtils;


@Service
public class WorkflowBatchService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowBatchService.class);
    private final WorkflowTaskBatchDao workflowTaskBatchDao;
    private final WorkflowDao workflowDao;
    private final WorkflowNodeDao workflowNodeDao;
    private final JobTaskBatchDao jobTaskBatchDao;
    private final WorkflowHandler workflowHandler;
    private final WorkflowBatchHandler workflowBatchHandler;
    private final JobDao jobDao;
    private final JobHandler jobHandler;
    private final WorkflowMapper workflowMapper;
    private final JobBatchResponseVOConverter jobBatchResponseVOConverter;
    private final JobResponseVOMapper jobResponseVOMapper;

    public WorkflowBatchService(WorkflowTaskBatchDao workflowTaskBatchDao, WorkflowDao workflowDao,
                                WorkflowNodeDao workflowNodeDao, JobTaskBatchDao jobTaskBatchDao,
                                WorkflowHandler workflowHandler, WorkflowBatchHandler workflowBatchHandler,
                                JobDao jobDao, JobHandler jobHandler, WorkflowMapper workflowMapper,
                                JobBatchResponseVOConverter jobBatchResponseVOConverter, JobResponseVOMapper jobResponseVOMapper) {
        this.workflowTaskBatchDao = workflowTaskBatchDao;
        this.workflowDao = workflowDao;
        this.workflowNodeDao = workflowNodeDao;
        this.jobTaskBatchDao = jobTaskBatchDao;
        this.workflowHandler = workflowHandler;
        this.workflowBatchHandler = workflowBatchHandler;
        this.jobDao = jobDao;
        this.jobHandler = jobHandler;
        this.workflowMapper = workflowMapper;
        this.jobBatchResponseVOConverter = jobBatchResponseVOConverter;
        this.jobResponseVOMapper = jobResponseVOMapper;
    }

    private static boolean isNoOperation(JobTaskBatch i) {
        return JobOperationReason.WORKFLOW_SUCCESSOR_SKIP_EXECUTION.contains(i.getOperationReason())
                || i.getTaskBatchStatus() == JobTaskBatchStatus.STOP;
    }

    public IPage<WorkflowBatchResponseVO> queryPage(Page<WorkflowTaskBatch> pageDTO, WorkflowBatchQuery queryVO) {

        //TODO 租户填充 表连接会生效吗

        QueryWrapper<WorkflowTaskBatch> wrapper = new QueryWrapper<WorkflowTaskBatch>()
                .eq("batch.namespace_id", TenantContext.getTenantId())
                .eq(queryVO.getWorkflowId() != null, "batch.workflow_id", queryVO.getWorkflowId())
                .eq(queryVO.getTaskBatchStatus() != null, "batch.task_batch_status", queryVO.getTaskBatchStatus())
                .likeRight(StrUtil.isNotBlank(queryVO.getWorkflowName()), "flow.workflow_name", queryVO.getWorkflowName())
                .eq("batch.deleted", 0)
                .orderByDesc("batch.id");
        List<WorkflowBatchResponseDO> batchResponseDOList = workflowTaskBatchDao.selectWorkflowBatchPageList(pageDTO,
                wrapper);

        List<WorkflowBatchResponseVO> batchResponseVOList =
                CollectionUtils.transformToList(batchResponseDOList, workflowMapper::convertWorkflowBatchResponseVO);
        var objectPage = new Page<WorkflowBatchResponseVO>();
        objectPage.setRecords(batchResponseVOList);
        objectPage.setCurrent(pageDTO.getCurrent());
        objectPage.setSize(pageDTO.getSize());
        return objectPage;
    }

    public WorkflowDetailResponseVO getWorkflowBatchDetail(BigInteger id) {

        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectOne(
                new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .eq(WorkflowTaskBatch::getId, id)
        );
        if (Objects.isNull(workflowTaskBatch)) {
            return null;
        }

        Workflow workflow = workflowDao.selectById(workflowTaskBatch.getWorkflowId());

        WorkflowDetailResponseVO responseVO = workflowMapper.convert(workflow);
        responseVO.setWorkflowBatchStatus(workflowTaskBatch.getTaskBatchStatus());
        List<WorkflowNode> workflowNodes = workflowNodeDao.selectList(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getDeleted, false)
                .eq(WorkflowNode::getWorkflowId, workflow.getId()));

        List<Job> jobs = jobDao.selectList(
                new LambdaQueryWrapper<Job>()
                        .in(Job::getId, StreamUtils.toSet(workflowNodes, WorkflowNode::getJobId)));

        Map<BigInteger, Job> jobMap = StreamUtils.toIdentityMap(jobs, Job::getId);

        List<JobTaskBatch> alJobTaskBatchList = jobTaskBatchDao.selectList(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getWorkflowTaskBatchId, id)
                        .orderByDesc(JobTaskBatch::getId));

        Map<BigInteger, List<JobTaskBatch>> jobTaskBatchMap = StreamUtils.groupByKey(alJobTaskBatchList,
                JobTaskBatch::getWorkflowNodeId);
        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = CollectionUtils.transformToList(workflowNodes, workflowMapper::convert);

        String flowInfo = workflowTaskBatch.getFlowInfo();
        MutableGraph<BigInteger> graph = MutableGraphCache.getOrDefault(id, flowInfo);

        Set<BigInteger> allNoOperationNode = Sets.newHashSet();
        Map<BigInteger, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
                .peek(nodeInfo -> {

                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    if (Objects.nonNull(jobTask)) {
                        jobTask.setJobName(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getJobName());
                    }

                    List<JobTaskBatch> jobTaskBatchList = jobTaskBatchMap.get(nodeInfo.getId());
                    if (CollectionUtils.isNotEmpty(jobTaskBatchList)) {
                        jobTaskBatchList = jobTaskBatchList.stream()
                                .sorted(Comparator.comparing(jobTaskBatch -> jobTaskBatch.getTaskBatchStatus().getValue()))
                                .collect(Collectors.toList());
                        nodeInfo.setJobBatchList(
                                CollectionUtils.transformToList(jobTaskBatchList, jobBatchResponseVOConverter::convert));

                        // 取第最新的一条状态
                        JobTaskBatch jobTaskBatch = jobTaskBatchList.get(0);
                        if (JobOperationReason.WORKFLOW_DECISION_FAILED
                                == jobTaskBatch.getOperationReason()) {
                            // 前端展示使用
                            nodeInfo.setTaskBatchStatus(JobTaskBatchStatus.WORKFLOW_DECISION_FAILED_STATUS);
                        } else {
                            nodeInfo.setTaskBatchStatus(jobTaskBatch.getTaskBatchStatus());
                        }

                        if (jobTaskBatchList.stream()
                                .filter(Objects::nonNull)
                                .anyMatch(WorkflowBatchService::isNoOperation)) {
                            // 当前节点下面的所有节点都是无需处理的节点
                            Set<BigInteger> allDescendants = MutableGraphCache.getAllDescendants(graph, nodeInfo.getId());
                            allNoOperationNode.addAll(allDescendants);
                        } else {
                            // 删除被误添加的节点
                            allNoOperationNode.remove(nodeInfo.getId());
                        }

                    } else {
                        if (JobTaskBatchStatus.NOT_SUCCESS.contains(workflowTaskBatch.getTaskBatchStatus())) {
                            allNoOperationNode.add(nodeInfo.getId());
                        }
                    }
                })
                .collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, Function.identity()));

        for (BigInteger noOperationNodeId : allNoOperationNode) {
            WorkflowDetailResponseVO.NodeInfo nodeInfo = workflowNodeMap.get(noOperationNodeId);
            List<JobTaskBatch> jobTaskBatches = jobTaskBatchMap.get(nodeInfo.getId());

            if (CollectionUtils.isNotEmpty(jobTaskBatches)) {
                jobTaskBatches = jobTaskBatches.stream()
                        .sorted(Comparator.comparing(jobTaskBatch -> jobTaskBatch.getTaskBatchStatus().getValue()))
                        .collect(Collectors.toList());
                nodeInfo.setJobBatchList(
                        CollectionUtils.transformToList(jobTaskBatches, jobBatchResponseVOConverter::convert));
            } else {
                JobBatchResponseVO jobBatchResponseVO = new JobBatchResponseVO();
                JobTaskConfig jobTask = nodeInfo.getJobTask();
                if (Objects.nonNull(jobTask)) {
                    jobBatchResponseVO.setJobId(jobTask.getJobId());
                }
                // 只为前端展示提供
//                nodeInfo.setTaskBatchStatus(NOT_HANDLE_STATUS);
//                jobBatchResponseVO.setTaskBatchStatus(NOT_HANDLE_STATUS);
//                jobBatchResponseVO.setOperationReason(JobOperationReasonEnum.WORKFLOW_NODE_NO_REQUIRED.getReason());
                nodeInfo.setJobBatchList(Lists.newArrayList(jobBatchResponseVO));
            }
        }

        try {
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = workflowHandler.buildNodeConfig(graph, SystemConstants.ROOT,
                    new HashMap<>(), workflowNodeMap);
            responseVO.setNodeConfig(config);
        } catch (Exception e) {
            log.error("反序列化失败. json:[{}]", flowInfo, e);
            throw new SilenceJobServerException("查询工作流批次详情失败");
        }

        return responseVO;
    }

    public Boolean stop(BigInteger id) {
        WorkflowTaskBatch workflowTaskBatch = workflowTaskBatchDao.selectById(id);
        Assert.notNull(workflowTaskBatch, () -> new SilenceJobServerException("workflow batch can not be null."));
        Assert.isTrue(JobTaskBatchStatus.NOT_COMPLETE.contains(workflowTaskBatch.getTaskBatchStatus()),
            () -> new SilenceJobServerException("workflow batch status completed."));

        workflowBatchHandler.stop(id, JobOperationReason.MANNER_STOP);
        return Boolean.TRUE;
    }

    @Transactional
    public Boolean deleteByIds(Set<BigInteger> ids) {

        Assert.isTrue(ids.size() == workflowTaskBatchDao.delete(new LambdaQueryWrapper<WorkflowTaskBatch>()
                        .in(WorkflowTaskBatch::getId, ids)),
                () -> new SilenceJobServerException("删除工作流任务失败, 请检查任务状态是否关闭状态"));

        List<JobTaskBatch> jobTaskBatches = jobTaskBatchDao.selectList(new LambdaQueryWrapper<JobTaskBatch>()
                .in(JobTaskBatch::getWorkflowTaskBatchId, ids));

        if (CollectionUtils.isEmpty(jobTaskBatches)) {
            return Boolean.TRUE;
        }

        Set<BigInteger> jobTaskBatchIds = StreamUtils.toSet(jobTaskBatches, JobTaskBatch::getId);
        jobHandler.deleteJobTaskBatchByIds(jobTaskBatchIds);

        return Boolean.TRUE;
    }

}
