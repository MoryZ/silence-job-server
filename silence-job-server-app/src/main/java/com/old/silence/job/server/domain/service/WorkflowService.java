package com.old.silence.job.server.domain.service;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.old.silence.job.common.constant.SystemConstants;
import com.old.silence.job.common.enums.JobTaskExecutorScene;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.expression.ExpressionEngine;
import com.old.silence.job.common.expression.ExpressionFactory;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.api.assembler.WorkflowMapper;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.dto.PartitionTask;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.util.CronUtils;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.common.util.GraphUtils;
import com.old.silence.job.server.common.util.PartitionTaskUtils;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.model.Job;
import com.old.silence.job.server.domain.model.JobSummary;
import com.old.silence.job.server.domain.model.Workflow;
import com.old.silence.job.server.domain.model.WorkflowNode;
import com.old.silence.job.server.dto.CheckDecisionVO;
import com.old.silence.job.server.dto.ExportWorkflowVO;
import com.old.silence.job.server.dto.JobTaskConfig;
import com.old.silence.job.server.dto.WorkflowCommand;
import com.old.silence.job.server.dto.WorkflowTriggerVO;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.handler.GroupHandler;
import com.old.silence.job.server.handler.WorkflowHandler;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobSummaryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowNodeDao;
import com.old.silence.job.server.infrastructure.persistence.dao.WorkflowNotifyConfigRelationDao;
import com.old.silence.job.server.job.task.dto.WorkflowTaskPrepareDTO;
import com.old.silence.job.server.job.task.support.WorkflowPrePareHandler;
import com.old.silence.job.server.job.task.support.WorkflowTaskConverter;
import com.old.silence.job.server.job.task.support.expression.ExpressionInvocationHandler;
import com.old.silence.job.server.vo.WorkflowDetailResponseVO;
import com.old.silence.job.server.vo.WorkflowResponseVO;

import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.server.vo.WorkflowView;


@Service

@Validated
public class WorkflowService  {


    private static final Logger log = LoggerFactory.getLogger(WorkflowService.class);
    private final WorkflowDao workflowDao;
    private final WorkflowNodeDao workflowNodeDao;
    private final SystemProperties systemProperties;
    private final WorkflowHandler workflowHandler;
    private final WorkflowPrePareHandler terminalWorkflowPrepareHandler;
    private final JobDao jobDao;
    private final GroupConfigDao groupConfigDao;
    private final GroupHandler groupHandler;
    private final JobSummaryDao jobSummaryDao;
    private final WorkflowNotifyConfigRelationDao workflowNotifyConfigRelationDao;
    private final WorkflowMapper workflowMapper;

    public WorkflowService(WorkflowDao workflowDao, WorkflowNodeDao workflowNodeDao,
                           SystemProperties systemProperties, WorkflowHandler workflowHandler,
                           WorkflowPrePareHandler terminalWorkflowPrepareHandler, JobDao jobDao,
                           GroupConfigDao groupConfigDao, GroupHandler groupHandler,
                           JobSummaryDao jobSummaryDao, WorkflowNotifyConfigRelationDao workflowNotifyConfigRelationDao, WorkflowMapper workflowMapper) {
        this.workflowDao = workflowDao;
        this.workflowNodeDao = workflowNodeDao;
        this.systemProperties = systemProperties;
        this.workflowHandler = workflowHandler;
        this.terminalWorkflowPrepareHandler = terminalWorkflowPrepareHandler;
        this.jobDao = jobDao;
        this.groupConfigDao = groupConfigDao;
        this.groupHandler = groupHandler;
        this.jobSummaryDao = jobSummaryDao;
        this.workflowNotifyConfigRelationDao = workflowNotifyConfigRelationDao;
        this.workflowMapper = workflowMapper;
    }

    private static Long calculateNextTriggerAt(Workflow workflow, Long time) {
        checkExecuteInterval(workflow);

        WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(workflow.getTriggerType().getValue());
        WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
        waitStrategyContext.setTriggerInterval(workflow.getTriggerInterval());
        waitStrategyContext.setNextTriggerAt(time);
        return waitStrategy.computeTriggerTime(waitStrategyContext);
    }

    private static void checkExecuteInterval(Workflow workflow) {
        if (List.of(WaitStrategies.WaitStrategyEnum.FIXED.getValue(),
                        WaitStrategies.WaitStrategyEnum.RANDOM.getValue())
                .contains(workflow.getTriggerType().getValue().intValue())) {
            if (Integer.parseInt(workflow.getTriggerInterval()) < 10) {
                throw new SilenceJobServerException("触发间隔不得小于10");
            }
        } else if (workflow.getTriggerType().getValue().intValue() == WaitStrategies.WaitStrategyEnum.CRON.getValue()) {
            if (CronUtils.getExecuteInterval(workflow.getTriggerInterval()) < 10 * 1000) {
                throw new SilenceJobServerException("触发间隔不得小于10");
            }
        }
    }

    @Transactional
    public boolean create(Workflow workflow, WorkflowCommand.NodeConfig nodeConfig) throws Exception {
        log.info("保存工作流信息：{}", JSON.toJSONString(workflow));
        MutableGraph<BigInteger> graph = createGraph();

        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 组装工作流信息
        workflow.setVersion(1);
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflow, DateUtils.toNowMilli()));
        workflow.setFlowInfo(StrUtil.EMPTY);
        workflow.setBucketIndex(
                HashUtil.bkdrHash(workflow.getGroupName() + workflow.getWorkflowName())
                        % systemProperties.getBucketTotal());
        workflow.setId(null);
        Assert.isTrue(1 == workflowDao.insert(workflow), () -> new SilenceJobServerException("新增工作流失败"));

        if (CollectionUtils.isNotEmpty(workflow.getNotifyRelations())) {
            workflow.getNotifyRelations().forEach(workflowNotifyConfigRelation-> {
                workflowNotifyConfigRelation.setWorkflowId(workflow.getId());
            });
            workflowNotifyConfigRelationDao.insertBatch(workflow.getNotifyRelations());
        }

        // 获取DAG节点配置

        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT),
                new LinkedBlockingDeque<>(),
                workflow.getGroupName(),
                workflow.getId(), nodeConfig, graph,
                workflow.getVersion());
        log.info("图构建完成. graph:[{}]", graph);

        // 保存图信息
        var flowInfoJson = JSON.toJSONString(GraphUtils.serializeGraphToJson(graph));

        LambdaUpdateWrapper<Workflow> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Workflow::getId, workflow.getId())
                .set(Workflow::getFlowInfo, flowInfoJson);

        Assert.isTrue(1 == workflowDao.update(null, updateWrapper), () -> new SilenceJobServerException("保存工作流图失败"));
        return true;
    }

    private MutableGraph<BigInteger> createGraph() {
        return GraphBuilder.directed()
                .nodeOrder(ElementOrder.sorted(BigInteger::compareTo))
                .incidentEdgeOrder(ElementOrder.stable())
                .allowsSelfLoops(false)
                .build();
    }

    public WorkflowDetailResponseVO findById(BigInteger id) {
        WorkflowView workflow = workflowDao.findById(id, WorkflowView.class).orElse(null);

        return doGetWorkflowDetail(workflow);
    }

    
    public IPage<WorkflowResponseVO> queryPage(Page<Workflow> pageDTO, QueryWrapper<Workflow> queryWrapper) {

        IPage<WorkflowView> resultPage = workflowDao.findByQuery( queryWrapper, pageDTO, WorkflowView.class);

        return resultPage.convert(workflowMapper::convertToWorkflow);
    }

    @Transactional
    public Boolean update(Workflow workflow, WorkflowCommand.NodeConfig nodeConfig) {
        var id = workflow.getId();
        Assert.notNull(id, () -> new SilenceJobServerException("工作流ID不能为空"));

        Assert.notNull(workflow, () -> new SilenceJobServerException("工作流不存在"));

        MutableGraph<BigInteger> graph = createGraph();

        // 添加虚拟头节点
        graph.addNode(SystemConstants.ROOT);

        // 获取DAG节点配置

        int version = workflow.getVersion();
        // 递归构建图
        workflowHandler.buildGraph(Lists.newArrayList(SystemConstants.ROOT), new LinkedBlockingDeque<>(),
                workflow.getGroupName(), id, nodeConfig, graph, version + 1);

        log.info("图构建完成. graph:[{}]", graph);

        // 保存图信息
        workflow.setVersion(version);
        workflow.setNextTriggerAt(calculateNextTriggerAt(workflow, DateUtils.toNowMilli()));
        workflow.setFlowInfo(JSON.toJSONString(GraphUtils.serializeGraphToJson(graph)));
        // 不允许更新组
        workflow.setGroupName(null);
        Assert.isTrue(
                workflowDao.update(workflow,
                        new LambdaQueryWrapper<Workflow>()
                                .eq(Workflow::getId, id)
                                .eq(Workflow::getVersion, version)) > 0,
                () -> new SilenceJobServerException("更新失败"));
        if (CollectionUtils.isNotEmpty(workflow.getNotifyRelations())) {
            workflowNotifyConfigRelationDao.deleteByWorkflowId(id);
            workflow.getNotifyRelations().forEach(workflowNotifyConfigRelation-> {
                workflowNotifyConfigRelation.setWorkflowId(id);
            });
            workflowNotifyConfigRelationDao.insertBatch(workflow.getNotifyRelations());
        } else {
            workflowNotifyConfigRelationDao.deleteByWorkflowId(id);
        }
        return Boolean.TRUE;
    }

    
    public Boolean updateStatus(BigInteger id, boolean workflowStatus) {
        Workflow workflow = workflowDao.selectOne(
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getWorkflowStatus)
                        .eq(Workflow::getId, id));
        Assert.notNull(workflow, () -> new SilenceJobServerException("工作流不存在"));

        workflow.setWorkflowStatus(workflowStatus);

        return 1 == workflowDao.updateById(workflow);
    }

    
    public Boolean trigger(WorkflowTriggerVO triggerVO) {
        Workflow workflow = workflowDao.selectById(triggerVO.getWorkflowId());
        Assert.notNull(workflow, () -> new SilenceJobServerException("workflow can not be null."));

        long count = groupConfigDao.selectCount(
                new LambdaQueryWrapper<GroupConfig>()
                        .eq(GroupConfig::getGroupName, workflow.getGroupName())
                        .eq(GroupConfig::getNamespaceId, workflow.getNamespaceId())
                        .eq(GroupConfig::getGroupStatus, true)
        );

        Assert.isTrue(count > 0,
                () -> new SilenceJobServerException("组:[{}]已经关闭，不支持手动执行.", workflow.getGroupName()));

        WorkflowTaskPrepareDTO prepareDTO = WorkflowTaskConverter.INSTANCE.toWorkflowTaskPrepareDTO(workflow);
        // 设置now表示立即执行
        prepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
        prepareDTO.setTaskExecutorScene(JobTaskExecutorScene.MANUAL_WORKFLOW);
        String tmpWfContext = triggerVO.getTmpWfContext();
        if (StrUtil.isNotBlank(tmpWfContext) && !JSON.isValid(tmpWfContext)){
            prepareDTO.setWfContext(tmpWfContext);
        }
        terminalWorkflowPrepareHandler.handler(prepareDTO);

        return Boolean.TRUE;
    }

    
    public List<WorkflowResponseVO> getWorkflowNameList(String keywords, BigInteger workflowId, String groupName) {
        IPage<WorkflowView> selectPage = workflowDao.findByQuery(
                new LambdaQueryWrapper<Workflow>()
                        .select(Workflow::getId, Workflow::getWorkflowName)
                        .likeRight(StrUtil.isNotBlank(keywords), Workflow::getWorkflowName, StrUtil.trim(keywords))
                        .eq(Objects.nonNull(workflowId), Workflow::getId, workflowId)
                        .eq(StrUtil.isNotBlank(groupName), Workflow::getGroupName, groupName)
                        .orderByDesc(Workflow::getId),
                new PageDTO<>(1, 100), WorkflowView.class);

        return CollectionUtils.transformToList(selectPage.getRecords(), workflowMapper::convertToWorkflow);
    }

    
    public Pair<Integer, Object> checkNodeExpression(CheckDecisionVO decisionVO) {
        try {
            ExpressionEngine realExpressionEngine = ExpressionFactory.getExpressionEngineByType(decisionVO.getExpressionType());
            Assert.notNull(realExpressionEngine, () -> new SilenceJobServerException("表达式引擎不存在"));
            ExpressionInvocationHandler invocationHandler = new ExpressionInvocationHandler(realExpressionEngine);
            ExpressionEngine expressionEngine = ExpressionFactory.getExpressionEngine(invocationHandler);
            Object eval = expressionEngine.eval(decisionVO.getNodeExpression(), decisionVO.getCheckContent());
            return Pair.of(200, eval);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("表达式异常. [{}]", decisionVO.getNodeExpression(), e);
            return Pair.of(500, e.getMessage());
        }
    }

    
    @Transactional(rollbackFor = Exception.class)
    public void importWorkflowTask(List<Workflow> requests) throws Exception {
        batchSaveWorkflowTask(requests);
    }

    
    public String exportWorkflowTask(ExportWorkflowVO exportVO) {

        List<WorkflowDetailResponseVO> resultList = new ArrayList<>();
        PartitionTaskUtils.process(startId -> {
            List<WorkflowView> workflowList = workflowDao.findByQuery(
                    new LambdaQueryWrapper<Workflow>()
                            .eq(StrUtil.isNotBlank(exportVO.getGroupName()), Workflow::getGroupName, exportVO.getGroupName())
                            .eq(Objects.nonNull(exportVO.getWorkflowStatus()), Workflow::getWorkflowStatus,
                                    exportVO.getWorkflowStatus())
                            .likeRight(StrUtil.isNotBlank(exportVO.getWorkflowName()), Workflow::getWorkflowName,
                                    exportVO.getWorkflowName())
                            .in(CollectionUtils.isNotEmpty(exportVO.getWorkflowIds()), Workflow::getId, exportVO.getWorkflowIds())
                            .ge(Workflow::getId, startId)
                            .orderByAsc(Workflow::getId)
            , new PageDTO<>(0, 100), WorkflowView.class).getRecords();
            return workflowList.stream()
                    .map(this::doGetWorkflowDetail)
                    .map(WorkflowPartitionTask::new)
                    .collect(Collectors.toList());
        }, partitionTasks -> {
            List<WorkflowPartitionTask> workflowPartitionTasks = (List<WorkflowPartitionTask>) partitionTasks;
            resultList.addAll(CollectionUtils.transformToList(workflowPartitionTasks, WorkflowPartitionTask::getResponseVO));
        }, 0);

        return JSON.toJSONString(resultList);
    }

    
    @Transactional
    public Boolean deleteByIds(Set<BigInteger> ids) {

        Assert.isTrue(ids.size() == workflowDao.delete(
                new LambdaQueryWrapper<Workflow>()
                        .eq(Workflow::getWorkflowStatus, false)
                        .in(Workflow::getId, ids)
        ), () -> new SilenceJobServerException("删除工作流任务失败, 请检查任务状态是否关闭状态"));

        List<JobSummary> jobSummaries = jobSummaryDao.selectList(new LambdaQueryWrapper<JobSummary>()
                .select(JobSummary::getId)
                .in(JobSummary::getBusinessId, ids)
                .eq(JobSummary::getSystemTaskType, SystemTaskType.WORKFLOW)
        );
        if (CollectionUtils.isNotEmpty(jobSummaries)) {
            Assert.isTrue(jobSummaries.size() ==
                            jobSummaryDao.deleteBatchIds(CollectionUtils.transformToSet(jobSummaries, JobSummary::getId)),
                    () -> new SilenceJobServerException("汇总表删除失败")
            );
        }
        return Boolean.TRUE;
    }

    private void batchSaveWorkflowTask(List<Workflow> workflows) throws Exception {

        Set<String> groupNameSet = CollectionUtils.transformToSet(workflows, Workflow::getGroupName);
        groupHandler.validateGroupExistence(groupNameSet);

        for (Workflow workflow : workflows) {
            checkExecuteInterval(workflow);
            create(workflow, null);
        }
    }

    private WorkflowDetailResponseVO doGetWorkflowDetail(WorkflowView workflow) {
        WorkflowDetailResponseVO responseVO = workflowMapper.convert(workflow);
        List<WorkflowNode> workflowNodes = workflowNodeDao.selectList(new LambdaQueryWrapper<WorkflowNode>()
                .eq(WorkflowNode::getVersion, workflow.getVersion())
                .eq(WorkflowNode::getWorkflowId, workflow.getId())
                .orderByAsc(WorkflowNode::getPriorityLevel));

        List<BigInteger> jobIds = CollectionUtils.transformToList(workflowNodes, WorkflowNode::getJobId);
        if (CollectionUtils.isEmpty(jobIds)) {
            return responseVO;
        }
        List<Job> jobs = jobDao.selectBatchIds(jobIds);

        Map<BigInteger, Job> jobMap = CollectionUtils.transformToMap(jobs, Job::getId);

        List<WorkflowDetailResponseVO.NodeInfo> nodeInfos = CollectionUtils.transformToList(workflowNodes, workflowMapper::convert);

        Map<BigInteger, WorkflowDetailResponseVO.NodeInfo> workflowNodeMap = nodeInfos.stream()
                .peek(nodeInfo -> {
                    JobTaskConfig jobTask = nodeInfo.getJobTask();
                    if (Objects.nonNull(jobTask)) {
                        jobTask.setJobName(jobMap.getOrDefault(jobTask.getJobId(), new Job()).getJobName());
                    }
                }).collect(Collectors.toMap(WorkflowDetailResponseVO.NodeInfo::getId, Function.identity()));

        String flowInfo = workflow.getFlowInfo();
        try {
            MutableGraph<BigInteger> graph = GraphUtils.deserializeJsonToGraph(flowInfo);
            // 反序列化构建图
            WorkflowDetailResponseVO.NodeConfig config = workflowHandler.buildNodeConfig(graph, SystemConstants.ROOT,
                    new HashMap<>(),
                    workflowNodeMap);
            responseVO.setNodeConfig(config);
        } catch (Exception e) {
            log.error("反序列化失败. json:[{}]", flowInfo, e);
            throw new SilenceJobServerException("查询工作流详情失败");
        }
        return responseVO;
    }

    private static class WorkflowPartitionTask extends PartitionTask {

        private final WorkflowDetailResponseVO responseVO;

        public WorkflowPartitionTask(@NotNull WorkflowDetailResponseVO responseVO) {
            this.responseVO = responseVO;
            setId(responseVO.getId());
        }

        public WorkflowDetailResponseVO getResponseVO() {
            return responseVO;
        }
    }
}
