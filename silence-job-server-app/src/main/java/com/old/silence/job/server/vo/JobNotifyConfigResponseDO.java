package com.old.silence.job.server.vo;



import java.time.Instant;

/**
 * @author zuoJunLin
 * @date 2023-12-02 23:03:01
 * @since 2.4.0
 */

public class JobNotifyConfigResponseDO {

    private Long id;

    private String namespaceId;

    private String groupName;

    private Long jobId;

    private String jobName;

    private Boolean notifyStatus;

    private Integer notifyType;

    private String notifyAttribute;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Boolean rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

    private Instant createdDate;

    private Instant updatedDate;

}
