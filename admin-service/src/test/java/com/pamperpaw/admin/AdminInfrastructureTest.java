package com.pamperpaw.admin;

import com.pamperpaw.admin.aspect.LoggingAspect;
import com.pamperpaw.admin.config.FeignInterceptor;
import com.pamperpaw.admin.dto.AdminDTO;
import com.pamperpaw.admin.dto.CustomerDTO;
import com.pamperpaw.admin.dto.PetDTO;
import com.pamperpaw.admin.dto.VetDTO;
import com.pamperpaw.admin.exception.DuplicateResourceException;
import com.pamperpaw.admin.exception.GlobalExceptionHandler;
import com.pamperpaw.admin.exception.ResourceNotFoundException;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminInfrastructureTest {

    @Test
    void mainDelegatesToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication =
                     org.mockito.Mockito.mockStatic(SpringApplication.class)) {

            AdminServiceApplication.main(new String[]{"arg"});

            springApplication.verify(() ->
                    SpringApplication.run(AdminServiceApplication.class, new String[]{"arg"}));
        }
    }

    @Test
    void loggingAspectHandlesSuccessAndFailure() throws Throwable {
        LoggingAspect aspect = new LoggingAspect();
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        Signature signature = mock(Signature.class);

        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.toShortString()).thenReturn("AdminService.method()");
        when(joinPoint.proceed()).thenReturn("ok");

        assertEquals("ok", aspect.logAround(joinPoint));

        when(joinPoint.proceed()).thenThrow(new IllegalStateException("boom"));
        assertThrows(IllegalStateException.class, () -> aspect.logAround(joinPoint));
    }

    @Test
    void globalExceptionHandlerBuildsResponses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request =
                new ServletWebRequest(new MockHttpServletRequest("GET", "/admin"));

        assertEquals(404,
                handler.handleNotFound(new ResourceNotFoundException("missing"), request)
                        .getStatusCode().value());

        assertEquals(409,
                handler.handleDuplicate(new DuplicateResourceException("duplicate"), request)
                        .getStatusCode().value());

        assertEquals(500,
                handler.handleGeneral(new IllegalStateException("boom"), request)
                        .getStatusCode().value());
    }

    @Test
    void globalExceptionHandlerHandlesValidationErrors() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(new FieldError("admin", "email", "Invalid email")));

        var response = handler.handleValidation(
                exception,
                new ServletWebRequest(new MockHttpServletRequest("POST", "/admin"))
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Invalid email",
                response.getBody().getValidationErrors().get("email"));
    }

    @Test
    void feignInterceptorCopiesAuthorizationHeaderWhenPresent() {
        FeignInterceptor interceptor = new FeignInterceptor();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer token");

        ReflectionTestUtils.setField(interceptor, "request", request);

        RequestTemplate template = new RequestTemplate();
        interceptor.apply(template);

        assertEquals("Bearer token",
                template.headers().get("Authorization").iterator().next());
    }

    @Test
    void adminDtoSupportsBuilderAndAccessors() {
        AdminDTO dto = AdminDTO.builder()
                .id(1L)
                .name("Admin")
                .email("admin@test.com")
                .password("secret")
                .role("ADMIN")
                .build();

        assertEquals(1L, dto.getId());
        assertEquals("Admin", dto.getName());
        assertEquals("admin@test.com", dto.getEmail());
        assertEquals("secret", dto.getPassword());
        assertEquals("ADMIN", dto.getRole());
    }

    @Test
    void dtoConstructorsAndAccessorsCoverFields() {
        AdminDTO admin = new AdminDTO();
        admin.setId(2L);
        admin.setName("Root");
        admin.setEmail("root@test.com");
        admin.setPassword("pw");
        admin.setRole("ADMIN");

        // ✅ FIXED: use builder instead of constructor
        CustomerDTO customer = CustomerDTO.builder()
                .id(3L)
                .name("Manu")
                .email("manu@test.com")
                .phone("9876543210")
                .address("Chennai")
                .build();

        PetDTO pet = PetDTO.builder()
                .id(4L)
                .name("Bruno")
                .type("Dog")
                .age(2)
                .build();

        VetDTO vet = VetDTO.builder()
                .id(5L)
                .name("Dr Paws")
                .specialization("Surgery")
                .experience(8)
                .phone("9123456789")
                .email("vet@test.com")
                .clinicAddress("Clinic Street")
                .availableDays("Mon-Fri")
                .availableTime("9AM-5PM")
                .build();

        assertEquals(2L, admin.getId());
        assertEquals("Root", admin.getName());

        assertEquals(3L, customer.getId());
        assertEquals("Manu", customer.getName());

        assertEquals(4L, pet.getId());
        assertEquals("Dog", pet.getType());

        assertEquals(5L, vet.getId());
        assertEquals("Dr Paws", vet.getName());
    }

    @Test
    void dtoBuildersCoverRemainingBoilerplate() {
        CustomerDTO customer = CustomerDTO.builder()
                .id(10L)
                .name("Alex")
                .email("alex@test.com")
                .phone("9999999999")
                .address("Bengaluru")
                .build();

        PetDTO pet = PetDTO.builder()
                .id(11L)
                .name("Milo")
                .type("Cat")
                .age(4)
                .build();

        VetDTO vet = VetDTO.builder()
                .id(12L)
                .name("Dr Care")
                .specialization("Dental")
                .experience(6)
                .phone("8888888888")
                .email("care@test.com")
                .clinicAddress("Health Street")
                .availableDays("Sat")
                .availableTime("10AM-2PM")
                .build();

        CustomerDTO mutableCustomer = new CustomerDTO();
        mutableCustomer.setName(customer.getName());

        PetDTO mutablePet = new PetDTO();
        mutablePet.setType(pet.getType());

        VetDTO mutableVet = new VetDTO();
        mutableVet.setName(vet.getName());

        assertEquals("Alex", mutableCustomer.getName());
        assertEquals("Cat", mutablePet.getType());
        assertEquals("Dr Care", mutableVet.getName());
    }
}