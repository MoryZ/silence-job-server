package com.old.silence.job.server.vo;



/**
 * @author MurrayZhang
 */

public class ConfigStatVo {
    private Integer namespaceCount;
    private Integer listenerInstanceCount;
    private Integer componentCount;

    public Integer getNamespaceCount() {
        return namespaceCount;
    }

    public void setNamespaceCount(Integer namespaceCount) {
        this.namespaceCount = namespaceCount;
    }

    public Integer getListenerInstanceCount() {
        return listenerInstanceCount;
    }

    public void setListenerInstanceCount(Integer listenerInstanceCount) {
        this.listenerInstanceCount = listenerInstanceCount;
    }

    public Integer getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(Integer componentCount) {
        this.componentCount = componentCount;
    }
}
