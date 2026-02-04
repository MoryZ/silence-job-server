package com.old.silence.job.server.api.config;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.old.silence.auth.center.security.SilenceAuthCenterContextHolder;
import com.old.silence.core.security.UserContextAware;

/**
 * @author moryzang
 */
@Configuration(proxyBeanMethods = false)
public class AuditorAwareConfiguration {

    private String getCurrentAuditor() {
        return SilenceAuthCenterContextHolder.getAuthenticatedUserName()
                .orElse("SYSTEM");
    }

    @Bean
    UserContextAware<String> userContextAware() {
        return () -> Optional.of(getCurrentAuditor());
    }
}
