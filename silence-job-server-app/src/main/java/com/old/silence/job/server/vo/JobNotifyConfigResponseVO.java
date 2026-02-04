package com.old.silence.job.server.vo;



import java.io.Serializable;
import java.time.Instant;

/**
 * @author: zuoJunLin
 * @date : 2023-12-02 11:22
 * @since : 2.5.0
 */

public class JobNotifyConfigResponseVO implements Serializable {

    private Long id;

    private String namespaceId;

    private String groupName;

    private Long jobId;

    private String jobName;

    private Boolean notifyStatus;

    private String notifyName;

    private Integer notifyType;

    private String notifyAttribute;

    private Integer notifyThreshold;

    private Integer notifyScene;

    private Boolean rateLimiterStatus;

    private Integer rateLimiterThreshold;

    private String description;

    private Instant createdDate;

    private Instant updatedDate;

    private static final long serialVersionUID = 1L;


}
