package com.old.silence.job.server.domain.service;


import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.HashUtil;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.common.enums.RetryStatus;
import com.old.silence.job.common.enums.SystemTaskType;
import com.old.silence.job.common.util.StreamUtils;
import com.old.silence.job.server.api.assembler.RetryDeadLetterResponseVOMapper;
import com.old.silence.job.server.common.WaitStrategy;
import com.old.silence.job.server.common.config.SystemProperties;
import com.old.silence.job.server.common.strategy.WaitStrategies.WaitStrategyContext;
import com.old.silence.job.server.common.strategy.WaitStrategies.WaitStrategyEnum;
import com.old.silence.job.server.domain.model.Retry;
import com.old.silence.job.server.domain.model.RetryDeadLetter;
import com.old.silence.job.server.domain.model.RetrySceneConfig;
import com.old.silence.job.server.dto.BatchDeleteRetryDeadLetterCommand;
import com.old.silence.job.server.dto.BatchRollBackRetryDeadLetterCommand;
import com.old.silence.job.server.exception.SilenceJobServerException;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetryDeadLetterDao;
import com.old.silence.job.server.infrastructure.persistence.dao.RetrySceneConfigDao;
import com.old.silence.job.server.retry.task.support.RetryTaskConverter;
import com.old.silence.job.server.vo.RetryDeadLetterResponseVO;


@Service
public class RetryDeadLetterService {
    private final RetryDeadLetterDao retryDeadLetterDao;
    private final RetrySceneConfigDao retrySceneConfigDao;
    private final RetryDao retryDao;
    private final SystemProperties systemProperties;
    private final RetryDeadLetterResponseVOMapper retryDeadLetterResponseVOMapper;

    public RetryDeadLetterService(RetryDeadLetterDao retryDeadLetterDao, RetrySceneConfigDao retrySceneConfigDao,
                                  RetryDao retryDao,
                                  SystemProperties systemProperties,
                                  RetryDeadLetterResponseVOMapper retryDeadLetterResponseVOMapper) {
        this.retryDeadLetterDao = retryDeadLetterDao;
        this.retrySceneConfigDao = retrySceneConfigDao;
        this.retryDao = retryDao;
        this.systemProperties = systemProperties;
        this.retryDeadLetterResponseVOMapper = retryDeadLetterResponseVOMapper;
    }

   
    public IPage<RetryDeadLetterResponseVO> queryPage(Page<RetryDeadLetter> pageDTO, QueryWrapper<RetryDeadLetter> queryWrapper) {

        Page<RetryDeadLetter> retryDeadLetterPage = retryDeadLetterDao
                .selectPage(pageDTO, queryWrapper);

        return retryDeadLetterPage.convert(retryDeadLetterResponseVOMapper::convert);
    }

   
    public RetryDeadLetterResponseVO findById(String groupName, BigInteger id) {
        RetryDeadLetter retryDeadLetter = retryDeadLetterDao.selectOne(new LambdaQueryWrapper<RetryDeadLetter>()
                .eq(RetryDeadLetter::getId, id)
                .eq(RetryDeadLetter::getGroupName, groupName)
        );
        return retryDeadLetterResponseVOMapper.convert(retryDeadLetter);
    }

   
    @Transactional
    public int rollback(BatchRollBackRetryDeadLetterCommand rollBackRetryDeadLetterVO) {


        List<BigInteger> ids = rollBackRetryDeadLetterVO.getIds();
        List<RetryDeadLetter> retryDeadLetterList = retryDeadLetterDao.selectList(
                new LambdaQueryWrapper<RetryDeadLetter>().in(RetryDeadLetter::getId, ids));

        Assert.notEmpty(retryDeadLetterList, () -> new SilenceJobServerException("数据不存在"));

        Set<String> sceneNameSet = CollectionUtils.transformToSet(retryDeadLetterList, RetryDeadLetter::getSceneName);
        List<RetrySceneConfig> retrySceneConfigs = retrySceneConfigDao.selectList(
                new LambdaQueryWrapper<RetrySceneConfig>()
                        .in(RetrySceneConfig::getSceneName, sceneNameSet));

        Map<String, RetrySceneConfig> sceneConfigMap = CollectionUtils.transformToMap(retrySceneConfigs,
                (sceneConfig) -> sceneConfig.getGroupName() + sceneConfig.getSceneName(), Function.identity());

        List<Retry> waitRollbackList = new ArrayList<>();
        for (RetryDeadLetter retryDeadLetter : retryDeadLetterList) {
            RetrySceneConfig retrySceneConfig = sceneConfigMap.get(
                    retryDeadLetter.getGroupName() + retryDeadLetter.getSceneName());
            Assert.notNull(retrySceneConfig,
                    () -> new SilenceJobServerException("未查询到场景. [{}]", retryDeadLetter.getSceneName()));

            Retry retry = RetryTaskConverter.INSTANCE.toRetryTask(retryDeadLetter);
            retry.setRetryStatus(RetryStatus.RUNNING);
            retry.setTaskType(SystemTaskType.RETRY);
            retry.setBucketIndex(HashUtil.bkdrHash(retryDeadLetter.getGroupName() + retryDeadLetter.getSceneName() + retryDeadLetter.getIdempotentId())
                    % systemProperties.getBucketTotal());
            retry.setParentId(BigInteger.ZERO);
            retry.setDeleted(false);

            WaitStrategyContext waitStrategyContext = new WaitStrategyContext();
            waitStrategyContext.setNextTriggerAt(Instant.now());
            waitStrategyContext.setTriggerInterval(retrySceneConfig.getTriggerInterval());
            waitStrategyContext.setDelayLevel(1);
            WaitStrategy waitStrategy = WaitStrategyEnum.getWaitStrategy(retrySceneConfig.getBackOff().getValue());
            retry.setNextTriggerAt(waitStrategy.computeTriggerTime(waitStrategyContext));
            retry.setCreatedDate(Instant.now());
            waitRollbackList.add(retry);
        }

        Assert.isTrue(waitRollbackList.size() == retryDao.insertBatch(waitRollbackList),
                () -> new SilenceJobServerException("新增重试任务失败"));

        Set<BigInteger> waitDelRetryDeadLetterIdSet = StreamUtils.toSet(retryDeadLetterList, RetryDeadLetter::getId);
        Assert.isTrue(waitDelRetryDeadLetterIdSet.size() == retryDeadLetterDao.delete(
                        new LambdaQueryWrapper<RetryDeadLetter>()
                                .in(RetryDeadLetter::getId, waitDelRetryDeadLetterIdSet)),
                () -> new SilenceJobServerException("删除死信队列数据失败"));
        return 1;
    }

   
    public boolean batchDelete(BatchDeleteRetryDeadLetterCommand deadLetterVO) {

        Assert.isTrue(deadLetterVO.getIds().size() == retryDeadLetterDao.delete(
                        new LambdaQueryWrapper<RetryDeadLetter>()
                                .in(RetryDeadLetter::getId, deadLetterVO.getIds())),
                () -> new SilenceJobServerException("删除死信任务失败"));

        return Boolean.TRUE;
    }
}
