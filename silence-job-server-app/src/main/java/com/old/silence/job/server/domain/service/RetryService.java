package com.old.silence.job.server.domain.service;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.StrUtil;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.old.silence.job.common.client.dto.GenerateRetryIdempotentIdDTO;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.RetryTaskExecutorSceneEnum;
import com.old.silence.job.common.enums.TaskGeneratorSceneEnum;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.model.ApiResult;
import com.old.silence.job.common.server.dto.RetryTaskDTO;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.api.assembler.RetryMapper;
import com.old.silence.job.server.api.assembler.RetryTaskResponseVOMapper;
import com.old.silence.job.server.api.assembler.TaskContextMapper;
import com.old.silence.job.server.api.config.TenantContext;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.cache.CacheRegisterTable;
import com.old.silence.job.server.common.dto.RegisterNodeInfo;
import com.old.silence.job.server.common.dto.RetryLogMetaDTO;
import com.old.silence.job.server.common.handler.ClientNodeAllocateHandler;
import com.old.silence.job.server.common.rpc.client.RequestBuilder;
import com.old.silence.job.server.common.strategy.WaitStrategies;
import com.old.silence.job.server.common.util.DateUtils;
import com.old.silence.job.server.domain.model.GroupConfig;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.domain.model.RetryTask;
import com.old.silence.job.server.domain.model.RetryTaskLogMessage;
import com.old.silence.job.server.dto.BatchDeleteRetryTaskVO;
import com.old.silence.job.server.dto.GenerateRetryIdempotentIdCommand;
import com.old.silence.job.server.dto.ManualTriggerTaskRequestVO;
import com.old.silence.job.server.dto.ParseLogsVO;
import com.old.silence.job.server.dto.RetrySaveRequestCommand;
import com.old.silence.job.server.dto.RetryUpdateExecutorNameRequestVO;
import com.old.silence.job.server.dto.RetryUpdateStatusRequestVO;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.GroupConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryTaskLogMessageDao;
import com.old.silence.job.server.common.pekko.ActorGenerator;
import com.old.silence.job.server.retry.task.client.RetryRpcClient;
import com.old.silence.job.server.retry.task.dto.RetryTaskPrepareDTO;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.retry.task.support.generator.retry.TaskContext;
import com.old.silence.job.server.retry.task.support.generator.retry.TaskGenerator;
import com.old.silence.job.server.vo.RetryResponseVO;

import com.old.silence.core.util.CollectionUtils;


@Service
public class RetryService {


    @Autowired
    @Lazy
    private List<TaskGenerator> taskGenerators;

    private final ClientNodeAllocateHandler clientNodeAllocateHandler;
    private final RetryDao retryDao;
    private final GroupConfigDao groupConfigDao;
    private final RetrySceneConfigDao retrySceneConfigDao;
    private final RetryTaskDao retryTaskDao;
    private final RetryTaskLogMessageDao retryTaskLogMessageDao;
    private final TransactionTemplate transactionTemplate;
    private final RetryTaskResponseVOMapper retryTaskResponseVOMapper;
    private final RetryMapper retryMapper;

    public RetryService(ClientNodeAllocateHandler clientNodeAllocateHandler, RetryDao retryDao,
                        GroupConfigDao groupConfigDao, RetrySceneConfigDao retrySceneConfigDao, RetryTaskDao retryTaskDao,
                        RetryTaskLogMessageDao retryTaskLogMessageDao,
                        TransactionTemplate transactionTemplate, RetryTaskResponseVOMapper retryTaskResponseVOMapper,
                        RetryMapper retryMapper) {
        this.clientNodeAllocateHandler = clientNodeAllocateHandler;
        this.retryDao = retryDao;
        this.groupConfigDao = groupConfigDao;
        this.retrySceneConfigDao = retrySceneConfigDao;
        this.retryTaskDao = retryTaskDao;
        this.retryTaskLogMessageDao = retryTaskLogMessageDao;
        this.transactionTemplate = transactionTemplate;
        this.retryTaskResponseVOMapper = retryTaskResponseVOMapper;
        this.retryMapper = retryMapper;
    }

    public IPage<RetryResponseVO> getRetryPage(Page<Retry> pageDTO, QueryWrapper<Retry> queryWrapper) {

        queryWrapper.lambda().eq(Retry::getTaskType, SystemTaskType.RETRY);
        pageDTO = retryDao.selectPage(pageDTO, queryWrapper);

        Set<BigInteger> ids = CollectionUtils.transformToSet(pageDTO.getRecords(), Retry::getId);
        Map<BigInteger, Retry> callbackMap = Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(ids)) {
            List<Retry> callbackTaskList = retryDao
                    .selectList(new LambdaQueryWrapper<Retry>().in(Retry::getParentId, ids));
           callbackMap = StreamUtils.toIdentityMap(callbackTaskList, Retry::getParentId);
        }

        Map<BigInteger, Retry> finalCallbackMap = callbackMap;
        return pageDTO.convert(retry-> convertToRetryResponseVO(retry, finalCallbackMap) );
    }

    private RetryResponseVO convertToRetryResponseVO(Retry retry, Map<BigInteger, Retry> callbackMap) {
        var retryResponseVO = retryTaskResponseVOMapper.convert(retry);
        RetryResponseVO childrenRetryResponseVO =  retryTaskResponseVOMapper.convert(callbackMap.get(retry.getId()));
        if (Objects.isNull(childrenRetryResponseVO)) {
            retryResponseVO.setChildren(Lists.newArrayList());
        } else {
            retryResponseVO.setChildren(Lists.newArrayList(childrenRetryResponseVO));
        }
        return retryResponseVO;
    }

    
    public RetryResponseVO findById(String groupName, BigInteger id) {
        var lambdaQueryWrapper = new LambdaQueryWrapper<Retry>().eq(Retry::getId, id);
        if (StringUtils.isNotBlank(groupName) || Objects.isNull(id)) {
            lambdaQueryWrapper.eq(Retry::getGroupName, groupName);
        }
        Retry retry = retryDao.selectOne(lambdaQueryWrapper);
        return retryTaskResponseVOMapper.convert(retry);
    }

    
    @Transactional
    public int updateRetryStatus(RetryUpdateStatusRequestVO requestVO) {

        RetryStatus retryStatus = requestVO.getRetryStatus();
        if (Objects.isNull(retryStatus)) {
            throw new SilenceJobServerException("重试状态错误. [{}]", requestVO.getRetryStatus());
        }


        Retry retry = retryDao.selectOne(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getId, requestVO.getId()));
        if (Objects.isNull(retry)) {
            throw new SilenceJobServerException("未查询到重试任务");
        }

        retry.setRetryStatus(requestVO.getRetryStatus());

        // 若恢复重试则需要重新计算下次触发时间
        if (RetryStatus.RUNNING.equals(retryStatus)) {

            RetrySceneConfig retrySceneConfig = getSceneConfigByGroupNameAndSceneName(retry.getGroupName(), retry.getSceneName());
            WaitStrategies.WaitStrategyContext waitStrategyContext = new WaitStrategies.WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(DateUtils.toNowMilli());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(retry.getRetryCount() + 1);
            WaitStrategy waitStrategy = WaitStrategies.WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff().getValue());
            retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
        }

        if (RetryStatus.FINISH.equals(retryStatus)) {
            RetryLogMetaDTO retryLogMetaDTO = RetryTaskConverter.INSTANCE.toLogMetaDTO(retry);
            retryLogMetaDTO.setTimestamp(DateUtils.toNowMilli());
            SilenceJobLog.REMOTE.info("=============手动操作完成============. <|>{}<|>", retryLogMetaDTO);
        }

        retry.setUpdatedDate(Instant.now());
        return retryDao.updateById(retry);
    }

    
    public int create(RetrySaveRequestCommand retrySaveRequestCommand) {
        RetryStatus retryStatus = retrySaveRequestCommand.getRetryStatus();
        if (Objects.isNull(retryStatus)) {
            throw new SilenceJobServerException("重试状态错误");
        }

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorSceneEnum.MANA_SINGLE.getScene()))
                .findFirst().orElseThrow(() -> new SilenceJobServerException("没有匹配的任务生成器"));

        TaskContext taskContext = new TaskContext();
        taskContext.setSceneName(retrySaveRequestCommand.getSceneName());
        taskContext.setGroupName(retrySaveRequestCommand.getGroupName());
        taskContext.setInitStatus(retrySaveRequestCommand.getRetryStatus());
        taskContext.setTaskInfos(
                Collections.singletonList(TaskContextMapper.INSTANCE.convert(retrySaveRequestCommand)));

        // 生成任务
        taskGenerator.taskGenerator(taskContext);

        return 1;
    }

    
    public String idempotentIdGenerate(GenerateRetryIdempotentIdCommand generateRetryIdempotentIdCommand) {

        var namespaceId = TenantContext.getTenantId();
        Set<RegisterNodeInfo> serverNodes = CacheRegisterTable.getServerNodeSet(
                generateRetryIdempotentIdCommand.getGroupName(),
                namespaceId);
        Assert.notEmpty(serverNodes,
                () -> new SilenceJobServerException("生成idempotentId失败: 不存在活跃的客户端节点"));

        RetrySceneConfig retrySceneConfig = getSceneConfigByGroupNameAndSceneName(generateRetryIdempotentIdCommand.getGroupName(),
                        generateRetryIdempotentIdCommand.getSceneName());

        RegisterNodeInfo serverNode = clientNodeAllocateHandler.getServerNode(retrySceneConfig.getSceneName(),
                retrySceneConfig.getGroupName(), retrySceneConfig.getNamespaceId(), retrySceneConfig.getRouteKey());

        // 委托客户端生成idempotentId
        GenerateRetryIdempotentIdDTO generateRetryIdempotentIdDTO = new GenerateRetryIdempotentIdDTO();
        generateRetryIdempotentIdDTO.setGroup(generateRetryIdempotentIdCommand.getGroupName());
        generateRetryIdempotentIdDTO.setScene(generateRetryIdempotentIdCommand.getSceneName());
        generateRetryIdempotentIdDTO.setArgsStr(generateRetryIdempotentIdCommand.getArgsStr());
        generateRetryIdempotentIdDTO.setExecutorName(generateRetryIdempotentIdCommand.getExecutorName());

        RetryRpcClient rpcClient = RequestBuilder.<RetryRpcClient, ApiResult>newBuilder()
                .nodeInfo(serverNode)
                .client(RetryRpcClient.class)
                .build();

        ApiResult result = rpcClient.generateIdempotentId(generateRetryIdempotentIdDTO);

        Assert.notNull(result, () -> new SilenceJobServerException("idempotentId生成失败"));
        Assert.isTrue(200 != result.getCode(),
                () -> new SilenceJobServerException("idempotentId生成失败:请确保参数与执行器名称正确"));

        return (String) result.getData();
    }

    
    public int updateRetryExecutorName(RetryUpdateExecutorNameRequestVO requestVO) {

        Retry retry = new Retry();
        retry.setExecutorName(requestVO.getExecutorName());
        retry.setRetryStatus(requestVO.getRetryStatus());
        retry.setUpdatedDate(Instant.now());

        // 根据重试数据id，更新执行器名称

        return retryDao.update(retry, new LambdaUpdateWrapper<Retry>()
                        .eq(Retry::getGroupName, requestVO.getGroupName())
                        .in(Retry::getId, requestVO.getIds()));
    }

    
    @Transactional
    public boolean batchDeleteRetry(BatchDeleteRetryTaskVO requestVO) {

        List<Retry> retries = retryDao.selectList(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getGroupName, requestVO.getGroupName())
                        .in(Retry::getRetryStatus, RetryStatus.ALLOW_DELETE_STATUS)
                        .in(Retry::getId, requestVO.getIds())
        );

        Assert.notEmpty(retries,
                () -> new SilenceJobServerException("没有可删除的数据, 只有非【处理中】的数据可以删除"));

        Set<BigInteger> retryIds = StreamUtils.toSet(retries, Retry::getId);
        retryTaskDao.delete(new LambdaQueryWrapper<RetryTask>()
                .eq(RetryTask::getGroupName, requestVO.getGroupName())
                .in(RetryTask::getRetryId, retryIds));

        retryTaskLogMessageDao.delete(
                new LambdaQueryWrapper<RetryTaskLogMessage>()
                        .eq(RetryTaskLogMessage::getGroupName, requestVO.getGroupName())
                        .in(RetryTaskLogMessage::getRetryId, retryIds));

        Assert.isTrue(requestVO.getIds().size() == retryDao.delete(new LambdaQueryWrapper<Retry>()
                                .eq(Retry::getGroupName, requestVO.getGroupName())
                                .in(Retry::getRetryStatus, RetryStatus.ALLOW_DELETE_STATUS)
                                .in(Retry::getId, requestVO.getIds()))
                , () -> new SilenceJobServerException("删除重试任务失败, 请检查任务状态是否为已完成或者最大次数"));

        return Boolean.TRUE;
    }

    
    public Integer parseLogs(ParseLogsVO parseLogsVO) {
        RetryStatus retryStatus = parseLogsVO.getRetryStatus();
        if (Objects.isNull(retryStatus)) {
            throw new SilenceJobServerException("重试状态错误");
        }

        String logStr = parseLogsVO.getLogStr();

        String patternString = "<\\|>(.*?)<\\|>";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(logStr);

        List<RetryTaskDTO> waitInsertList = new ArrayList<>();
        // 查找匹配的内容并输出
        while (matcher.find()) {
            String extractedData = matcher.group(1);
            if (StrUtil.isBlank(extractedData)) {
                continue;
            }

            List<RetryTaskDTO> retryTaskList = JSON.parseArray(extractedData, RetryTaskDTO.class);
            if (CollectionUtils.isNotEmpty(retryTaskList)) {
                waitInsertList.addAll(retryTaskList);
            }
        }

        Assert.isFalse(waitInsertList.isEmpty(), () -> new SilenceJobServerException("未找到匹配的数据"));
        Assert.isTrue(waitInsertList.size() <= 500, () -> new SilenceJobServerException("最多只能处理500条数据"));

        TaskGenerator taskGenerator = taskGenerators.stream()
                .filter(t -> t.supports(TaskGeneratorSceneEnum.MANA_BATCH.getScene()))
                .findFirst().orElseThrow(() -> new SilenceJobServerException("没有匹配的任务生成器"));

        boolean allMatch = waitInsertList.stream()
                .allMatch(retryTaskDTO -> retryTaskDTO.getGroupName().equals(parseLogsVO.getGroupName()));
        Assert.isTrue(allMatch, () -> new SilenceJobServerException("存在数据groupName不匹配，请检查您的数据"));

        Map<String, List<RetryTaskDTO>> map = StreamUtils.groupByKey(waitInsertList, RetryTaskDTO::getSceneName);


        transactionTemplate.execute((status -> {
            map.forEach(((sceneName, retryTaskDTOS) -> {
                TaskContext taskContext = new TaskContext();
                taskContext.setSceneName(sceneName);
                taskContext.setGroupName(parseLogsVO.getGroupName());
                taskContext.setInitStatus(parseLogsVO.getRetryStatus());
                taskContext.setTaskInfos(CollectionUtils.transformToList(retryTaskDTOS, TaskContextMapper.INSTANCE::convert));

                // 生成任务
                try {
                    taskGenerator.taskGenerator(taskContext);
                } catch (DuplicateKeyException e) {
                    throw new SilenceJobServerException("namespaceId:[{}] groupName:[{}] sceneName:[{}] 任务已经存在",
                            TenantContext.getTenantId(), parseLogsVO.getGroupName(), sceneName);
                }

            }));
            return Boolean.TRUE;
        }));

        return waitInsertList.size();
    }

    
    public boolean manualTriggerRetryTask(ManualTriggerTaskRequestVO requestVO) {

        long count = groupConfigDao.selectCount(new LambdaQueryWrapper<GroupConfig>()
                .eq(GroupConfig::getGroupName, requestVO.getGroupName())
                .eq(GroupConfig::getGroupStatus, true)
        );

        Assert.isTrue(count > 0, () -> new SilenceJobServerException("组:[{}]已经关闭，不支持手动执行.", requestVO.getGroupName()));

        List<BigInteger> retryIds = requestVO.getRetryIds();

        List<Retry> list = retryDao.selectList(new LambdaQueryWrapper<Retry>()
                        .eq(Retry::getTaskType, SystemTaskType.RETRY)
                        .in(Retry::getId, retryIds)
        );
        Assert.notEmpty(list, () -> new SilenceJobServerException("没有可执行的任务"));

        for (Retry retry : list) {
            RetryTaskPrepareDTO retryTaskPrepareDTO = retryMapper.toRetryTaskPrepareDTO(retry);
            // 设置now表示立即执行
            retryTaskPrepareDTO.setNextTriggerAt(DateUtils.toNowMilli());
            retryTaskPrepareDTO.setRetryTaskExecutorScene(RetryTaskExecutorSceneEnum.MANUAL_RETRY.getScene());
            retryTaskPrepareDTO.setRetryId(retry.getId());
            // 准备阶段执行
            ActorRef actorRef = ActorGenerator.retryTaskPrepareActor();
            actorRef.tell(retryTaskPrepareDTO, actorRef);
        }

        return true;
    }

    private RetrySceneConfig getSceneConfigByGroupNameAndSceneName(String groupName, String sceneName) {
        var queryWrapper = new LambdaQueryWrapper<RetrySceneConfig>();
        queryWrapper.eq(RetrySceneConfig::getGroupName, groupName);
        queryWrapper.eq(RetrySceneConfig::getSceneName, sceneName);
        return retrySceneConfigDao.selectOne(queryWrapper);
    }
}
