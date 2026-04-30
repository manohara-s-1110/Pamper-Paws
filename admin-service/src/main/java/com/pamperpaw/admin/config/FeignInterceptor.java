package com.pamperpaw.admin.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignInterceptor implements RequestInterceptor {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service-Key";

    @Autowired
    private HttpServletRequest request;

    @Value("${internal.service.key}")
    private String internalServiceKey;

    @Override
    public void apply(RequestTemplate template) {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null) {
            template.header("Authorization", authHeader);
        }

        template.header(INTERNAL_SERVICE_HEADER, internalServiceKey);
    }
}
