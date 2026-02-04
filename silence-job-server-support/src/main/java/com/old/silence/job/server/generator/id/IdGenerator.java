package com.old.silence.job.server.common.generator.id;

import com.old.silence.job.common.enums.IdGeneratorMode;

/**
 * 分布式Id生成器
 *
 */
public interface IdGenerator {

    /**
     * 获取匹配的模式
     *
     * @param mode 1. 雪花算法(默认算法) 2.号段模式
     */
    boolean supports(IdGeneratorMode mode);

    /**
     * 获取分布式id
     *
     * @param groupName   组
     * @param namespaceId 命名空间
     * @return id
     */
    String idGenerator(String groupName, String namespaceId);

}
