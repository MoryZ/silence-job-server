package com.old.silence.job.server.dto;

import jakarta.validation.constraints.NotBlank;
import java.math.BigInteger;



public class NamespaceCommand {

    private BigInteger id;

    /**
     * 命名空间唯一标识
     */
    private String uniqueId;

    /**
     * 名称
     */
    @NotBlank(message = "name 不能为空")
    private String name;

    private String description;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
