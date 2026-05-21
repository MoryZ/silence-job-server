package com.old.silence.job.server.listener;

import com.alibaba.fastjson2.JSON;
import com.old.silence.job.server.common.enums.JobLogWebSocketSceneEnum;
import com.old.silence.job.server.domain.dto.JobLogQueryDTO;
import com.old.silence.job.server.event.WsRequestEvent;
import com.old.silence.job.server.service.JobLogWebSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * WebSocket 请求监听器
 */
@Component
public class JobLogWsRequestListener {


    private static final Logger log = LoggerFactory.getLogger(JobLogWsRequestListener.class);
    private final JobLogWebSocketService jobLogWebSocketService;

    public JobLogWsRequestListener(JobLogWebSocketService jobLogWebSocketService) {
        this.jobLogWebSocketService = jobLogWebSocketService;
    }

    @Async("logQueryExecutor")
    @EventListener(classes = WsRequestEvent.class)
    public void handleJobLogRequest(WsRequestEvent requestEvent) {
        if (!JobLogWebSocketSceneEnum.JOB_LOG_SCENE.equals(requestEvent.getSceneEnum())) {
            return;
        }

        log.info("Received job log request. sid:[{}]", requestEvent.getSid());
        try {
            JobLogQueryDTO queryDTO = JSON.parseObject(requestEvent.getMessage(), JobLogQueryDTO.class);
            queryDTO.setSid(requestEvent.getSid());
            queryDTO.setStartRealTime(0L);
            jobLogWebSocketService.pullJobLogs(queryDTO);
        } catch (Exception e) {
            log.error("Failed to handle job log request", e);
        }
    }
}
