package com.pamperpaw.vet;

import com.pamperpaw.vet.aspect.LoggingAspect;
import com.pamperpaw.vet.entity.Vet;
import com.pamperpaw.vet.exception.GlobalExceptionHandler;
import com.pamperpaw.vet.exception.VetNotFoundException;
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

class VetInfrastructureTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = org.mockito.Mockito.mockStatic(SpringApplication.class)) {
            VetServiceApplication.main(new String[]{"arg"});

            springApplication.verify(() -> SpringApplication.run(VetServiceApplication.class, new String[]{"arg"}));
        }
    }

    @Test
    void loggingAspectHandlesSuccessAndFailure() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("VetService.method()");
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.logAround(joinPoint));

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, () -> aspect.logAround(joinPoint));
    }

    @Test
    void globalExceptionHandlerBuildsResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/vets/1"));

        assertEquals(404, handler.handleVetNotFound(new VetNotFoundException("missing"), request).getStatusCode().value());
        assertEquals(500, handler.handleGeneral(new IllegalStateException("boom"), request).getStatusCode().value());
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("vet", "email", "Invalid email")));

        var response = handler.handleValidation(exception, new ServletWebRequest(new MockHttpServletRequest("POST", "/vets")));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid email", response.getBody().getValidationErrors().get("email"));
    }

    @Test
    void vetEntityStoresValues() {
        Vet vet = Vet.builder()
                .id(1L)
                .name("Dr Paws")
                .specialization("Surgery")
                .experience(5)
                .phone("9876543210")
                .email("vet@test.com")
                .clinicAddress("Clinic")
                .availableDays("Mon")
                .availableTime("10AM")
                .build();

        assertEquals("Dr Paws", vet.getName());
        assertEquals("Clinic", vet.getClinicAddress());
    }
}
