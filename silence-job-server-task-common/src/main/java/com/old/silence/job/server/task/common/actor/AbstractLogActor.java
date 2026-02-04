package com.old.silence.job.server.task.common.actor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.old.silence.core.util.CollectionUtils;
import com.old.silence.job.log.dto.TaskLogFieldDTO;
import org.apache.pekko.actor.AbstractActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 日志 Actor 抽象基类
 * 统一 Job 和 Retry 模块的日志处理逻辑
 *
 * @param <T> 批量日志 DTO 类型
 * @param <D> 单条日志 DTO 类型
 * @param <M> 日志消息实体类型
 * @author mory
 */
public abstract class AbstractLogActor<T, D, M> extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(AbstractLogActor.class);

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(List.class, this::handleBatchLog)
                .match(getSingleLogClass(), this::handleSingleLog)
                .build();
    }

    /**
     * 处理批量日志
     */
    private void handleBatchLog(List<?> list) {
        try {
            if (CollectionUtils.isEmpty(list)) {
                return;
            }

            List<T> logTasks = (List<T>) list;
            Map<BigInteger, List<T>> logTaskMap = logTasks.stream()
                    .collect(Collectors.groupingBy(this::getTaskId, Collectors.toList()));

            List<M> logMessages = new ArrayList<>();
            for (List<T> logTaskList : logTaskMap.values()) {
                M logMessage = createLogMessage(logTaskList.get(0));
                populateBatchLogMessage(logMessage, logTaskList);
                logMessages.add(logMessage);
            }

            insertBatch(logMessages);
        } catch (Exception e) {
            log.error("保存日志异常", e);
        } finally {
            getContext().stop(getSelf());
        }
    }

    /**
     * 处理单条日志
     */
    private void handleSingleLog(Object singleLog) {
        try {
            D logDTO = (D) singleLog;
            M logMessage = createSingleLogMessage(logDTO);
            insert(logMessage);
        } catch (Exception e) {
            log.error("保存日志异常", e);
        } finally {
            getContext().stop(getSelf());
        }
    }

    /**
     * 填充批量日志消息内容
     */
    protected void populateBatchLogMessage(M logMessage, List<T> logTaskList) {
        setCreatedDate(logMessage, Instant.now());
        setLogNum(logMessage, logTaskList.size());
        
        List<Map<String, String>> messageMapList = logTaskList.stream()
                .map(taskDTO -> getFieldList(taskDTO).stream()
                        .filter(field -> !Objects.isNull(field.getValue()))
                        .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue)))
                .collect(Collectors.toList());
        
        setMessage(logMessage, JSON.toJSONString(messageMapList));
    }

    /**
     * 获取单条日志的 Class（用于类型匹配）
     */
    protected abstract Class<D> getSingleLogClass();

    /**
     * 从批量日志 DTO 中获取任务 ID
     */
    protected abstract BigInteger getTaskId(T logTask);

    /**
     * 从批量日志 DTO 中获取字段列表
     */
    protected abstract List<TaskLogFieldDTO> getFieldList(T logTask);

    /**
     * 从批量日志 DTO 创建日志消息实体
     */
    protected abstract M createLogMessage(T logTask);

    /**
     * 从单条日志 DTO 创建日志消息实体
     */
    protected abstract M createSingleLogMessage(D logDTO);

    /**
     * 设置创建时间
     */
    protected abstract void setCreatedDate(M logMessage, Instant createdDate);

    /**
     * 设置日志数量
     */
    protected abstract void setLogNum(M logMessage, int logNum);

    /**
     * 设置消息内容
     */
    protected abstract void setMessage(M logMessage, String message);

    /**
     * 批量插入日志
     */
    protected abstract void insertBatch(List<M> logMessages);

    /**
     * 插入单条日志
     */
    protected abstract void insert(M logMessage);

    /**
     * 获取字符串值或空字符串
     */
    protected String getStringOrEmpty(String value) {
        return Optional.ofNullable(value).filter(StrUtil::isNotBlank).orElse(StrUtil.EMPTY);
    }

    /**
     * 获取 BigInteger 值或 ZERO
     */
    protected BigInteger getBigIntegerOrZero(BigInteger value) {
        return Optional.ofNullable(value).orElse(BigInteger.ZERO);
    }
}
