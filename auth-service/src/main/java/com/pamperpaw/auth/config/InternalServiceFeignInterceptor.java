package com.pamperpaw.auth.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InternalServiceFeignInterceptor implements RequestInterceptor {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service-Key";

    private final String internalServiceKey;

    public InternalServiceFeignInterceptor(@Value("${internal.service.key}") String internalServiceKey) {
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    public void apply(RequestTemplate template) {
        template.header(INTERNAL_SERVICE_HEADER, internalServiceKey);
    }
}
