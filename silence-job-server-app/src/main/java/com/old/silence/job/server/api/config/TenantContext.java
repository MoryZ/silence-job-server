package com.old.silence.job.server.api.config;

/**
 * @author moryzang
 */
public class TenantContext {

    private static final ThreadLocal<String> TENANT_CONTEXT = new ThreadLocal<>();

    private TenantContext() {
        // 工具类，防止实例化
    }

    /**
     * 设置当前租户ID
     */
    public static void setTenantId(String tenantId) {
        TENANT_CONTEXT.set(tenantId);
    }

    /**
     * 获取当前租户ID
     */
    public static String getTenantId() {
        return TENANT_CONTEXT.get();
    }

    /**
     * 清除当前租户上下文
     */
    public static void clear() {
        TENANT_CONTEXT.remove();
    }
}
