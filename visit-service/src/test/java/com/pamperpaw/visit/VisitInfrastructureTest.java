package com.pamperpaw.visit;

import com.pamperpaw.visit.aspect.LoggingAspect;
import com.pamperpaw.visit.exception.GlobalExceptionHandler;
import com.pamperpaw.visit.exception.ResourceNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisitInfrastructureTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
            VisitServiceApplication.main(new String[]{"arg"});

            springApplication.verify(() -> SpringApplication.run(VisitServiceApplication.class, new String[]{"arg"}));
        }
    }

    @Test
    void loggingAspectHandlesSuccessAndFailure() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("VisitService.method()");
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.logAround(joinPoint));

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, () -> aspect.logAround(joinPoint));
    }

    @Test
    void globalExceptionHandlerBuildsResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/visit/1"));

        assertEquals(404, handler.handleNotFound(new ResourceNotFoundException("missing"), request).getStatusCode().value());
        assertEquals(500, handler.handleGeneralException(new IllegalStateException("boom"), request).getStatusCode().value());
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("visit", "reason", "Reason is required")));

        var response = handler.handleValidationExceptions(exception, new ServletWebRequest(new MockHttpServletRequest("POST", "/visit")));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Reason is required", response.getBody().getValidationErrors().get("reason"));
    }
}
