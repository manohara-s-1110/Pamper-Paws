package com.pamperpaw.visit;

import com.pamperpaw.visit.aspect.LoggingAspect;
import com.pamperpaw.visit.exception.GlobalExceptionHandler;
import com.pamperpaw.visit.exception.ResourceNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VisitInfrastructureTest {

    @Test
    void loggingAspectHandlesSuccessAndFailure() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();

        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("VisitService.method()");

        when(joinPoint.proceed()).thenReturn("ok");

        Object result = aspect.logAround(joinPoint);

        assertEquals("ok", result);

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));

        assertThrows(IllegalStateException.class,
                () -> aspect.logAround(joinPoint));
    }

    @Test
    void globalExceptionHandlerBuildsResponses() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        ServletWebRequest request =
                new ServletWebRequest(
                        new MockHttpServletRequest("GET", "/visit/1"));

        var notFoundResponse =
                handler.handleNotFound(
                        new ResourceNotFoundException("missing"),
                        request);

        assertEquals(404, notFoundResponse.getStatusCode().value());
        assertNotNull(notFoundResponse.getBody());

        var generalResponse =
                handler.handleGeneralException(
                        new IllegalStateException("boom"),
                        request);

        assertEquals(500, generalResponse.getStatusCode().value());
        assertNotNull(generalResponse.getBody());
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrors() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "visit");

        bindingResult.addError(
                new FieldError(
                        "visit",
                        "reason",
                        "Reason is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        var response =
                handler.handleValidationExceptions(
                        exception,
                        new ServletWebRequest(
                                new MockHttpServletRequest("POST", "/visit")));

        assertEquals(400, response.getStatusCode().value());

        assertEquals(
                "Reason is required",
                response.getBody()
                        .getValidationErrors()
                        .get("reason"));
    }

    @Test
    void globalExceptionHandlerHandlesMultipleValidationErrors() {

        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        BeanPropertyBindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "visit");

        bindingResult.addError(
                new FieldError(
                        "visit",
                        "reason",
                        "Reason is required"));

        bindingResult.addError(
                new FieldError(
                        "visit",
                        "date",
                        "Date is required"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        var response =
                handler.handleValidationExceptions(
                        exception,
                        new ServletWebRequest(
                                new MockHttpServletRequest("POST", "/visit")));

        assertEquals(400, response.getStatusCode().value());

        assertEquals(
                2,
                response.getBody().getValidationErrors().size());

        assertTrue(
                response.getBody()
                        .getValidationErrors()
                        .containsKey("reason"));

        assertTrue(
                response.getBody()
                        .getValidationErrors()
                        .containsKey("date"));
    }
}