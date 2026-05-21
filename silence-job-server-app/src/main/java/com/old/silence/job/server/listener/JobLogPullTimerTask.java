package com.old.silence.job.server.listener;

import com.old.silence.job.log.SilenceJobLog;
import com.old.silence.job.server.common.TimerTask;
import com.old.silence.job.server.domain.dto.JobLogQueryDTO;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 日志拉取定时任务
 */
public class JobLogPullTimerTask implements TimerTask<String> {

    private static final String IDEMPOTENT_KEY_PREFIX = "jobLog_{0}_{1}";
    private static final Duration DELAY_MILLS = Duration.ofMillis(5000L);

    private final JobLogQueryDTO queryDTO;
    private final String sid;

    public JobLogPullTimerTask(JobLogQueryDTO queryDTO, String sid) {
        this.queryDTO = queryDTO;
        this.sid = sid;
    }

    @Override
    public void run(io.netty.util.Timeout timeout) throws Exception {
        SilenceJobLog.LOCAL.debug("Start querying scheduled task logs. Current time:[{}] taskBatchId:[{}]",
                LocalDateTime.now(), queryDTO.getTaskBatchId());

        try {
            LogTimerWheel.removeCache(idempotentKey());
            // 使用 JobLogWebSocketService 继续拉取日志
            com.old.silence.job.server.service.JobLogWebSocketService service =
                    com.old.silence.job.common.context.SilenceSpringContext.getContext().getBean(com.old.silence.job.server.service.JobLogWebSocketService.class);
            service.pullJobLogs(queryDTO);
        } catch (Exception e) {
            SilenceJobLog.LOCAL.error("Scheduled task log query execution failed", e);
        }
    }

    @Override
    public String idempotentKey() {
        return MessageFormat.format(IDEMPOTENT_KEY_PREFIX, sid, queryDTO.getTaskBatchId());
    }

    public static Duration getDelayMills() {
        return DELAY_MILLS;
    }
}
