package com.pamperpaw.customer;

import com.pamperpaw.customer.aspect.LoggingAspect;
import com.pamperpaw.customer.entity.Customer;
import com.pamperpaw.customer.entity.Pet;
import com.pamperpaw.customer.exception.DuplicateResourceException;
import com.pamperpaw.customer.exception.GlobalExceptionHandler;
import com.pamperpaw.customer.exception.ResourceNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CustomerInfrastructureTest {

    @Test
    void loggingAspectHandlesSuccessAndFailure() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("CustomerService.method()");
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.logAround(joinPoint));

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, () -> aspect.logAround(joinPoint));
    }

    @Test
    void globalExceptionHandlerBuildsResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = new ServletWebRequest(new MockHttpServletRequest("GET", "/customers/1"));

        assertEquals(404, handler.handleResourceNotFound(new ResourceNotFoundException("missing"), request).getStatusCode().value());
        assertEquals(409, handler.handleDuplicateResource(new DuplicateResourceException("duplicate"), request).getStatusCode().value());
        assertEquals(500, handler.handleGeneralException(new IllegalStateException("boom"), request).getStatusCode().value());
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "customer");
        bindingResult.addError(new FieldError("customer", "email", "Invalid email"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        var response = handler.handleValidationException(exception, new ServletWebRequest(new MockHttpServletRequest("POST", "/customers")));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid email", response.getBody().getValidationErrors().get("email"));
    }

    @Test
    void petEntityStoresValues() {
        Customer customer = new Customer();
        customer.setId(2L);
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setName("Bruno");
        pet.setType("Dog");
        pet.setAge(3);
        pet.setCustomer(customer);

        assertEquals(1L, pet.getId());
        assertEquals("Bruno", pet.getName());
        assertEquals("Dog", pet.getType());
        assertEquals(3, pet.getAge());
        assertEquals(customer, pet.getCustomer());
    }
}
