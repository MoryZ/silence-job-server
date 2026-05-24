package com.old.silence.job.server.service;

import com.alibaba.fastjson2.JSON;
import com.old.silence.job.log.constant.LogFieldConstants;
import com.old.silence.job.common.enums.JobTaskBatchStatus;
import com.old.silence.job.server.domain.dto.JobLogQueryDTO;
import com.old.silence.job.server.domain.model.JobLogMessage;
import com.old.silence.job.server.domain.model.JobTaskBatch;
import com.old.silence.job.server.event.WsSendEvent;
import com.old.silence.job.server.infrastructure.persistence.dao.JobLogMessageDao;
import com.old.silence.job.server.infrastructure.persistence.dao.JobTaskBatchDao;
import com.old.silence.job.common.context.SilenceSpringContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * WebSocket 日志推送服务
 */
@Service
public class JobLogWebSocketService {


    private static final Logger log = LoggerFactory.getLogger(JobLogWebSocketService.class);
    private final JobLogMessageDao jobLogMessageDao;
    private final JobTaskBatchDao jobTaskBatchDao;

    public JobLogWebSocketService(JobLogMessageDao jobLogMessageDao, JobTaskBatchDao jobTaskBatchDao) {
        this.jobLogMessageDao = jobLogMessageDao;
        this.jobTaskBatchDao = jobTaskBatchDao;
    }

    /**
     * 拉取任务日志并通过 WebSocket 推送
     *
     * @param queryDTO 查询条件
     */
    public void pullJobLogs(JobLogQueryDTO queryDTO) {
        String sid = queryDTO.getSid();
        BigInteger taskBatchId = queryDTO.getTaskBatchId();

        log.info("Pulling job logs. sid:[{}], taskBatchId:[{}], startRealTime:[{}]",
                sid, taskBatchId, queryDTO.getStartRealTime());

        // 查询日志
        List<JobLogMessage> jobLogMessages = jobLogMessageDao.selectList(
                new LambdaQueryWrapper<JobLogMessage>()
                        .eq(JobLogMessage::getTaskBatchId, taskBatchId)
                        .ge(Objects.nonNull(queryDTO.getStartRealTime()) && queryDTO.getStartRealTime() > 0,
                                JobLogMessage::getRealTime, queryDTO.getStartRealTime())
                        .orderByAsc(JobLogMessage::getRealTime)
                        .last("LIMIT 50")
        );

        long lastRealTime = queryDTO.getStartRealTime() != null ? queryDTO.getStartRealTime() : 0L;

        if (!jobLogMessages.isEmpty()) {
            for (JobLogMessage jobLogMessage : jobLogMessages) {
                lastRealTime = jobLogMessage.getRealTime();
                // 解析日志消息
                String message = jobLogMessage.getMessage();
                List<Map<String, String>> logContents = JSON.parseObject(message, List.class);
                if (logContents != null) {
                    logContents = logContents.stream()
                            .sorted(Comparator.comparingLong(o -> Long.parseLong(o.getOrDefault(LogFieldConstants.TIME_STAMP, "0"))))
                            .collect(Collectors.toList());
                    for (Map<String, String> logContent : logContents) {
                        // 发送日志到前端
                        sendLog(sid, JSON.toJSONString(logContent));
                    }
                }
            }
        }

        // 检查任务是否完成
        JobTaskBatch jobTaskBatch = jobTaskBatchDao.selectOne(
                new LambdaQueryWrapper<JobTaskBatch>()
                        .eq(JobTaskBatch::getId, taskBatchId)
        );

        // 结束查询条件：任务不存在 或 任务已完成 且 更新时间超过15秒
        boolean shouldEnd = Objects.isNull(jobTaskBatch)
                || (JobTaskBatchStatus.COMPLETED.contains(jobTaskBatch.getTaskBatchStatus())
                && jobTaskBatch.getUpdatedDate().plusSeconds(15).isBefore(Instant.now()));

        if (shouldEnd) {
            // 发送结束标识
            sendLog(sid, "\"END\"");
            log.info("Job log pull completed. sid:[{}], taskBatchId:[{}]", sid, taskBatchId);
        } else {
            // 设置下次查询的起始时间
            queryDTO.setStartRealTime(lastRealTime);
            // 调度下次查询（5秒后）
            com.old.silence.job.server.listener.JobLogPullTimerTask timerTask =
                    new com.old.silence.job.server.listener.JobLogPullTimerTask(queryDTO, sid);
            com.old.silence.job.server.listener.LogTimerWheel.registerWithJobLog(
                    () -> timerTask,
                    com.old.silence.job.server.listener.JobLogPullTimerTask.getDelayMills()
            );
        }
    }

    /**
     * 发送日志到 WebSocket
     */
    private void sendLog(String sid, String message) {
        WsSendEvent sendEvent = new WsSendEvent(this);
        sendEvent.setSid(sid);
        sendEvent.setMessage(message);
        SilenceSpringContext.getContext().publishEvent(sendEvent);
    }
}
