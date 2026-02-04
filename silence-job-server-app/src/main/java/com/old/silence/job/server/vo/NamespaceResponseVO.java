package com.old.silence.job.server.vo;


import java.math.BigInteger;
import java.time.Instant;

public class NamespaceResponseVO {

    private BigInteger id;

    /**
     * 名称
     */
    private String name;

    /**
     * 唯一id
     */
    private String uniqueId;

    private String description;

    /**
     * 创建时间
     */
    private Instant createdDate;

    /**
     * 修改时间
     */
    private Instant updatedDate;

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    public Instant getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = updatedDate;
    }
}
