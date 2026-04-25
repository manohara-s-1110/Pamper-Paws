package com.pamperpaw.auth;

import com.pamperpaw.auth.aspect.LoggingAspect;
import com.pamperpaw.auth.entity.User;
import com.pamperpaw.auth.exception.GlobalExceptionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthInfrastructureTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
            AuthServiceApplication.main(new String[]{"arg"});

            springApplication.verify(() -> SpringApplication.run(AuthServiceApplication.class, new String[]{"arg"}));
        }
    }

    @Test
    void loggingAspectHandlesSuccessAndFailure() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("AuthService.method()");
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.logAround(joinPoint));

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, () -> aspect.logAround(joinPoint));
    }

    @Test
    void globalExceptionHandlerBuildsResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("POST", "/auth/login"));

        assertEquals(401, handler.handleResponseStatus(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "bad"), request).getStatusCode().value());
        assertEquals(500, handler.handleGeneral(new IllegalStateException("boom"), request).getStatusCode().value());
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("register", "username", "Username is required")));

        var response = handler.handleValidation(exception, new ServletWebRequest(new MockHttpServletRequest("POST", "/auth/register")));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Username is required", response.getBody().getValidationErrors().get("username"));
    }

    @Test
    void userEntityStoresValues() {
        User user = new User();
        user.setId(1L);
        user.setUsername("manu");
        user.setPassword("secret");
        user.setRole("ADMIN");

        assertEquals(1L, user.getId());
        assertEquals("manu", user.getUsername());
        assertEquals("ADMIN", user.getRole());
    }
}
