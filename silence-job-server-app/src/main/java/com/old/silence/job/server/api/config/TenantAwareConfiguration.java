package com.old.silence.job.server.api.config;

import java.util.Optional;

import com.old.silence.core.security.TenantContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author moryzang
 */
@Configuration(proxyBeanMethods = false)
public class TenantAwareConfiguration {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Bean
    TenantContextAware<String> tenantContextAware() {
        return () -> Optional.ofNullable(resolveTenantFromHeader());
    }

    private String resolveTenantFromHeader() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(request -> request.getHeader(TENANT_HEADER))
                .filter(StringUtils::hasText)
                .orElse(null);
    }
}
