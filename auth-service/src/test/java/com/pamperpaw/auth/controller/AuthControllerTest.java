package com.pamperpaw.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.auth.dto.ChangePasswordRequest;
import com.pamperpaw.auth.dto.LoginRequest;
import com.pamperpaw.auth.dto.RegisterRequest;
import com.pamperpaw.auth.dto.VetRegisterRequest;
import com.pamperpaw.auth.exception.GlobalExceptionHandler;
import com.pamperpaw.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void registerDelegatesToService() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("manu");
        request.setPassword("Secret@123");
        request.setRole("CUSTOMER");
        request.setName("Manu");

        when(authService.register(request)).thenReturn("User registered successfully");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered successfully"));

        verify(authService).register(request);
    }

    @Test
    void registerReturnsValidationErrorForBlankUsername() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("");
        request.setPassword("Secret@123");
        request.setRole("CUSTOMER");
        request.setName("Manu");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.username").value("Username is required"));
    }

    @Test
    void loginDelegatesToService() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setUsername("manu");
        request.setPassword("secret");

        when(authService.login(request)).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));

        verify(authService).login(request);
    }

    @Test
    void loginWithParamsDelegatesToService() throws Exception {
        when(authService.login(org.mockito.ArgumentMatchers.any(LoginRequest.class))).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .param("username", "manu")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(content().string("jwt-token"));
    }

    @Test
    void registerVetDelegatesToService() throws Exception {
        VetRegisterRequest request = new VetRegisterRequest();
        request.setUsername("vet1");
        request.setPassword("Secret@123");
        request.setName("Dr Vet");
        request.setPhone("9876543210");
        request.setEmail("vet@example.com");
        request.setSpecialization("Surgery");
        request.setExperience(4);
        request.setClinicAddress("Chennai");
        request.setAvailableDays("Mon");
        request.setAvailableTime("10 AM - 1 PM");
        request.setConsultationFee(java.math.BigDecimal.valueOf(500));

        when(authService.registerVet(request)).thenReturn("Veterinarian account created successfully");

        mockMvc.perform(post("/auth/register/vet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Veterinarian account created successfully"));
    }

    @Test
    void changePasswordDelegatesToService() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("newpass");

        when(authService.changePassword("Bearer token", request)).thenReturn("Password updated successfully");

        mockMvc.perform(put("/auth/password")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }
}
