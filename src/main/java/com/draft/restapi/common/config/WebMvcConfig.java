package com.draft.restapi.common.config;

import com.draft.restapi.common.ratelimit.RateLimitingService;
import com.draft.restapi.common.ratelimit.RateLimitInterceptor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@ConditionalOnBean(RateLimitingService.class) // required for SliceTest
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitingService rateLimitingService;

    public WebMvcConfig(RateLimitingService rateLimitingService) {
        this.rateLimitingService = rateLimitingService;
    }

    @Bean
    @NonNull
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor(rateLimitingService);
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**");
    }
}
