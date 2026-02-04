package com.old.silence.job.server.dto;


import com.old.silence.data.commons.annotation.RelationalQueryProperty;
import com.old.silence.data.commons.converter.Part;

public class NamespaceQueryVO  {

    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String name;

    @RelationalQueryProperty(type = Part.Type.STARTING_WITH)
    private String uniqueId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
