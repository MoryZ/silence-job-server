package com.old.silence.job.server.common.log;

import com.old.silence.job.log.enums.LogTypeEnum;
import com.old.silence.job.server.common.LogStorage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public final class LogStorageFactory {

    private LogStorageFactory() {
    }

    private static final Map<LogTypeEnum, LogStorage> LOG_STORAGE = new ConcurrentHashMap<>();

    public static void register(LogTypeEnum logType, LogStorage logStorage) {
        LOG_STORAGE.put(logType, logStorage);
    }

    public static LogStorage get(LogTypeEnum logType) {
        return LOG_STORAGE.get(logType);
    }
}
