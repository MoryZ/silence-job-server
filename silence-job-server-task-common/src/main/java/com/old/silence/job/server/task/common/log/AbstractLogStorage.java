package com.old.silence.job.server.task.common.log;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.old.silence.job.log.dto.LogContentDTO;
import com.old.silence.job.log.dto.TaskLogFieldDTO;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.server.common.LogStorage;
import com.old.silence.job.server.common.dto.LogMetaDTO;
import com.old.silence.job.server.common.log.LogStorageFactory;
import org.apache.pekko.actor.ActorRef;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 日志存储抽象基类（模板方法模式）
 * 统一 Job 和 Retry 模块的日志存储逻辑
 *
 * @param <D> DTO 类型
 * @param <M> Meta DTO 类型
 * @author mory
 */
public abstract class AbstractLogStorage<D, M extends LogMetaDTO> implements LogStorage, InitializingBean {

    /**
     * 模板方法：存储日志
     */
    @Override
    public void storage(LogContentDTO logContentDTO, LogMetaDTO logMetaDTO) {
        // 1. 类型转换
        @SuppressWarnings("unchecked")
        M metaDTO = (M) logMetaDTO;

        // 2. 创建 DTO
        D dto = createLogDTO();

        // 3. 转换消息内容（公共逻辑）
        Map<String, String> messageMap = logContentDTO.getFieldList()
                .stream()
                .filter(field -> !Objects.isNull(field.getValue()))
                .collect(Collectors.toMap(TaskLogFieldDTO::getName, TaskLogFieldDTO::getValue));
        String message = JSON.toJSONString(Lists.newArrayList(messageMap));

        // 4. 填充 DTO（由子类实现）
        populateLogDTO(dto, message, logContentDTO, metaDTO);

        // 5. 获取 Actor 并发送（由子类实现）
        ActorRef actorRef = getLogActor();
        actorRef.tell(dto, actorRef);
    }

    /**
     * 注册到工厂（公共逻辑）
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        LogStorageFactory.register(logType(), this);
    }

    /**
     * 创建日志 DTO 实例（由子类实现）
     *
     * @return DTO 实例
     */
    protected abstract D createLogDTO();

    /**
     * 填充日志 DTO（由子类实现）
     *
     * @param dto            DTO 实例
     * @param message        消息内容
     * @param logContentDTO  日志内容
     * @param metaDTO        元数据
     */
    protected abstract void populateLogDTO(D dto, String message, LogContentDTO logContentDTO, M metaDTO);

    /**
     * 获取日志 Actor（由子类实现）
     *
     * @return Actor 引用
     */
    protected abstract ActorRef getLogActor();

    /**
     * 日志类型（由子类实现）
     *
     * @return 日志类型枚举
     */
    @Override
    public abstract LogTypeEnum logType();
}
