package com.old.silence.job.server.dto;

import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;
import com.old.silence.job.common.enums.ExecutorType;

/**
 * @author moryzang
 */
public class JobExecutorQuery {

    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String groupName;
    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String executorInfo;
    /**
     * 1:java; 2:python; 3:go;
     */
    @RelationalQueryProperty(type = Part.Type.SIMPLE_PROPERTY)
    private ExecutorType executorType;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getExecutorInfo() {
        return executorInfo;
    }

    public void setExecutorInfo(String executorInfo) {
        this.executorInfo = executorInfo;
    }

    public ExecutorType getExecutorType() {
        return executorType;
    }

    public void setExecutorType(ExecutorType executorType) {
        this.executorType = executorType;
    }
}
