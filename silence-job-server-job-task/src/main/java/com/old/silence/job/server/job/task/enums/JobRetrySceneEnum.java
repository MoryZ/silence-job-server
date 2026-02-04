package com.old.silence.job.server.job.task.enums;


public enum JobRetrySceneEnum {

    AUTO(1),
    MANUAL(2);

    private final Integer retryScene;

    JobRetrySceneEnum(Integer retryScene) {
        this.retryScene = retryScene;
    }

    public Integer getRetryScene() {
        return retryScene;
    }
}
