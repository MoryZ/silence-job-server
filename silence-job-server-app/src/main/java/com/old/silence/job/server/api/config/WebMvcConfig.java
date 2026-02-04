package com.old.silence.job.server.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author moryzang
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor())
                .addPathPatterns("/**")  // 拦截所有路径
                .excludePathPatterns("/error", "/static/**"); // 排除一些路径
    }

    @Bean
    public TenantInterceptor tenantInterceptor() {
        return new TenantInterceptor();
    }
}