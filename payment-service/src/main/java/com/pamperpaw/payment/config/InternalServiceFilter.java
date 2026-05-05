package com.pamperpaw.payment.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalServiceFilter extends OncePerRequestFilter {

    private static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service-Key";
    private final String internalServiceKey;

    public InternalServiceFilter(@Value("${internal.service.key}") String internalServiceKey) {
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (HttpMethod.OPTIONS.matches(request.getMethod()) || isPublicInfrastructureRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestKey = request.getHeader(INTERNAL_SERVICE_HEADER);
        if (!internalServiceKey.equals(requestKey)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Direct service access is not allowed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicInfrastructureRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        return "/actuator/health".equals(path)
                || "/swagger-ui.html".equals(path)
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs/")
                || "/v3/api-docs".equals(path);
    }
}
