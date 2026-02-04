package com.old.silence.job.server.common.dto;



import java.time.Instant;
import java.util.List;



public class FilterStrategyContext {

    private Long id;

    private RegisterNodeInfo registerNodeInfo;

    private String groupName;

    private Instant nextTriggerAt;


    private String uniqueId;

    private List<String> sceneBlacklist;

    private String sceneName;


}
