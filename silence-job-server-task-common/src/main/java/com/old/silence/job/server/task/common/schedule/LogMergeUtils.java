package com.old.silence.job.server.task.common.schedule;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.old.silence.core.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志合并工具类
 * 提供通用的日志合并、分区处理方法
 *
 * @author mory
 */
public class LogMergeUtils {

    private LogMergeUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 合并日志消息
     *
     * @param logMessages   日志消息列表
     * @param messageGetter 日志消息提取器
     * @param idGetter      ID 提取器
     * @param <T>           日志消息类型
     * @return 合并结果 (删除的ID列表, 合并后的消息列表)
     */
    public static <T> MergeResult mergeLogs(List<T> logMessages,
                                            MessageGetter<T> messageGetter,
                                            IdGetter<T> idGetter) {
        List<BigInteger> deleteIds = new ArrayList<>();
        
        List<String> mergedMessages = logMessages.stream()
                .map(msg -> {
                    deleteIds.add(idGetter.getId(msg));
                    return JSON.parseObject(messageGetter.getMessage(msg), List.class);
                })
                .reduce((a, b) -> {
                    List<String> list = new ArrayList<>();
                    list.addAll(a);
                    list.addAll(b);
                    return list;
                })
                .orElse(new ArrayList<>());

        return new MergeResult(deleteIds, mergedMessages);
    }

    /**
     * 分区消息
     *
     * @param messages      消息列表
     * @param partitionSize 分区大小
     * @return 分区后的消息列表
     */
    public static List<List<String>> partitionMessages(List<String> messages, int partitionSize) {
        if (CollectionUtils.isEmpty(messages)) {
            return new ArrayList<>();
        }
        return Lists.partition(messages, partitionSize);
    }

    /**
     * 日志消息提取器
     */
    @FunctionalInterface
    public interface MessageGetter<T> {
        String getMessage(T logMessage);
    }

    /**
     * ID 提取器
     */
    @FunctionalInterface
    public interface IdGetter<T> {
        BigInteger getId(T logMessage);
    }

    /**
     * 合并结果
     */
    public static class MergeResult {
        private final List<BigInteger> deleteIds;
        private final List<String> mergedMessages;

        public MergeResult(List<BigInteger> deleteIds, List<String> mergedMessages) {
            this.deleteIds = deleteIds;
            this.mergedMessages = mergedMessages;
        }

        public List<BigInteger> getDeleteIds() {
            return deleteIds;
        }

        public List<String> getMergedMessages() {
            return mergedMessages;
        }
    }
}
