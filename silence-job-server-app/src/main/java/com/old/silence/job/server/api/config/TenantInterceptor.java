package com.old.silence.job.server.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author moryzang
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {
    private static final String TENANT_HEADER = "X-Tenant-Id";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader(TENANT_HEADER);
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            TenantContext.setTenantId(tenantId.trim());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }
}