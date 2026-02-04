package com.old.silence.job.server.common;


import com.old.silence.job.log.dto.LogContentDTO;
import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.server.common.dto.LogMetaDTO;

public interface LogStorage {

    LogTypeEnum logType();

    void storage(LogContentDTO logContentDTO, LogMetaDTO logMetaDTO);
}
