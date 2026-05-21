package com.old.silence.job.server.common.enums;


import com.old.silence.core.enums.DescribedEnumValue;
import com.old.silence.core.enums.EnumValueFactory;

import java.util.Arrays;

/**
 * WebSocket 场景枚举
 */
public enum JobLogWebSocketSceneEnum implements DescribedEnumValue<Byte> {
    JOB_LOG_SCENE(1, "JOB_LOG_SCENE"),
    WORKFLOW_LOG_SCENE(2, "WORKFLOW_LOG_SCENE"),
    RETRY_LOG_SCENE(3, "RETRY_LOG_SCENE");

    private final byte value;
    private final String description;

    JobLogWebSocketSceneEnum(int value, String description) {
        this.value = (byte) value;
        this.description = description;
    }

    public static JobLogWebSocketSceneEnum valueOfScene(String scene) {
        return Arrays.stream(JobLogWebSocketSceneEnum.values()).filter(x -> x.description.equals(scene)).findFirst().orElse(null);
    }


    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public Byte getValue() {
        return value;
    }
}
