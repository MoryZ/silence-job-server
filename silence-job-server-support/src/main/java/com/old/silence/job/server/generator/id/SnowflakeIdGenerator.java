package com.old.silence.job.server.common.generator.id;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;
import com.old.silence.job.common.enums.IdGeneratorMode;

/**
 * 使用hutool自带的雪花算法生成id
 * 若出现时间回拨问题则直接报错 {@link Snowflake#tilNextMillis(long)}
 *
 */
@Component
public class SnowflakeIdGenerator implements IdGenerator {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake();

    @Override
    public boolean supports(IdGeneratorMode mode) {
        return IdGeneratorMode.SNOWFLAKE.equals(mode);
    }

    @Override
    public String idGenerator(String group, String namespaceId) {
        return SNOWFLAKE.nextIdStr();
    }
}
