package com.pamperpaw.visit.config;

import feign.RequestTemplate;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class ConfigTest {

    @Test
    void feignInterceptorAddsHeader() {

        InternalServiceFeignInterceptor interceptor =
                new InternalServiceFeignInterceptor("secret-key");

        RequestTemplate template = new RequestTemplate();

        interceptor.apply(template);

        assertTrue(
                template.headers()
                        .containsKey("X-Internal-Service-Key"));
    }

    @Test
    void filterAllowsOptionsRequest() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("OPTIONS");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void filterAllowsSwaggerRequests() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/swagger-ui/index.html");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void filterAllowsHealthEndpoint() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/actuator/health");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void filterRejectsInvalidHeader() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/visit");

        request.addHeader(
                "X-Internal-Service-Key",
                "wrong-key");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertTrue(
                response.getStatus() == 403);
    }

    @Test
    void filterAllowsValidHeader() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/visit");

        request.addHeader(
                "X-Internal-Service-Key",
                "secret-key");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void filterAllowsLocalhostRequests() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", true);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/visit");

        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void filterHandlesIpv6Loopback() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", true);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/visit");

        request.setRemoteAddr("::1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(any(), any());
    }

    @Test
    void filterRejectsWhenLocalRequestsDisabled() throws Exception {

        InternalServiceFilter filter =
                new InternalServiceFilter("secret-key", false);

        MockHttpServletRequest request =
                new MockHttpServletRequest();

        request.setMethod("GET");

        request.setRequestURI("/visit");

        request.setRemoteAddr("127.0.0.1");

        MockHttpServletResponse response =
                new MockHttpServletResponse();

        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertTrue(response.getStatus() == 403);
    }

    @Test
    void interceptorConstructorCoverage() {

        assertDoesNotThrow(() ->
                new InternalServiceFeignInterceptor(
                        "secret-key"));
    }
}