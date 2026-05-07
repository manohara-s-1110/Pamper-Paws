package com.pamperpaw.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.auth.client.CustomerClient;
import com.pamperpaw.auth.client.VetClient;
import com.pamperpaw.auth.dto.ChangePasswordRequest;
import com.pamperpaw.auth.dto.LoginRequest;
import com.pamperpaw.auth.dto.RegisterRequest;
import com.pamperpaw.auth.dto.VetRegisterRequest;
import com.pamperpaw.auth.entity.User;
import com.pamperpaw.auth.repository.UserRepository;
import com.pamperpaw.auth.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private VetClient vetClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerCreatesUserWithEncodedPasswordAndUppercaseRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("  manu ");
        request.setPassword("secret");
        request.setRole("customer");
        request.setName("Manu");
        request.setEmail("manu@example.com");
        request.setPhone("9876543210");
        request.setAddress("Coimbatore");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(customerClient.createCustomer(any())).thenReturn(new Object());

        String response = authService.register(request);

        assertEquals("User registered successfully", response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerRejectsDuplicateUsername() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("manu");
        request.setPassword("secret");
        request.setRole("CUSTOMER");
        request.setName("Manu");
        request.setEmail("manu@example.com");
        request.setPhone("9876543210");
        request.setAddress("Coimbatore");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(new User()));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerRejectsUnsupportedRole() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("manu");
        request.setPassword("secret");
        request.setRole("GROOMER");
        request.setName("Manu");

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerRejectsAdminRoleFromPublicEndpoint() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("adminuser");
        request.setPassword("secret");
        request.setRole("ADMIN");
        request.setName("Admin User");

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerRejectsVetRoleFromCustomerEndpoint() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("vetuser");
        request.setPassword("secret");
        request.setRole("VET");

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void registerRejectsMissingCustomerProfileFields() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("manu");
        request.setPassword("secret");
        request.setRole("CUSTOMER");
        request.setName("Manu");
        request.setEmail("");
        request.setPhone("9876543210");
        request.setAddress("Coimbatore");

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void registerRollsBackAuthUserWhenCustomerServiceFails() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("manu");
        request.setPassword("secret");
        request.setRole("CUSTOMER");
        request.setName("Manu");
        request.setEmail("manu@example.com");
        request.setPhone("9876543210");
        request.setAddress("Coimbatore");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(customerClient.createCustomer(any())).thenThrow(new RuntimeException("down"));
        when(customerClient.getCustomerByUsername("manu")).thenThrow(new RuntimeException("missing"));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.register(request));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        verify(userRepository).deleteByUsername("manu");
    }

    @Test
    void registerTreatsExistingCustomerProfileAsSuccessAfterDownstreamFailure() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("manu");
        request.setPassword("secret");
        request.setRole("CUSTOMER");
        request.setName("Manu");
        request.setEmail("manu@example.com");
        request.setPhone("9876543210");
        request.setAddress("Coimbatore");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(customerClient.createCustomer(any())).thenThrow(new RuntimeException("timeout"));
        when(customerClient.getCustomerByUsername("manu")).thenReturn(new Object());

        assertEquals("User registered successfully", authService.register(request));
        verify(userRepository, never()).deleteByUsername("manu");
    }

    @Test
    void registerVetCreatesAuthUserAndVetProfile() {
        VetRegisterRequest request = new VetRegisterRequest();
        request.setUsername("vet1");
        request.setPassword("secret");
        request.setName("Dr Vet");
        request.setPhone("9876543210");
        request.setEmail("vet@example.com");
        request.setSpecialization("Surgery");
        request.setExperience(6);
        request.setClinicAddress("Chennai");
        request.setAvailableDays("Mon");
        request.setAvailableTime("10 AM - 1 PM");
        request.setConsultationFee(java.math.BigDecimal.valueOf(500));

        when(userRepository.findByUsername("vet1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(vetClient.createVet(any())).thenReturn(new Object());

        assertEquals("Veterinarian account created successfully", authService.registerVet(request));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerVetRollsBackWhenVetServiceFails() {
        VetRegisterRequest request = new VetRegisterRequest();
        request.setUsername("vet1");
        request.setPassword("secret");
        request.setName("Dr Vet");
        request.setPhone("9876543210");
        request.setEmail("vet@example.com");
        request.setSpecialization("Surgery");
        request.setExperience(6);
        request.setClinicAddress("Chennai");
        request.setAvailableDays("Mon");
        request.setAvailableTime("10 AM - 1 PM");
        request.setConsultationFee(java.math.BigDecimal.valueOf(500));

        when(userRepository.findByUsername("vet1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(vetClient.createVet(any())).thenThrow(new RuntimeException("down"));
        when(vetClient.getVetByUsername("vet1")).thenThrow(new RuntimeException("missing"));

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.registerVet(request));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        verify(userRepository).deleteByUsername("vet1");
    }

    @Test
    void registerVetTreatsExistingVetProfileAsSuccessAfterDownstreamFailure() {
        VetRegisterRequest request = vetRegisterRequest();

        when(userRepository.findByUsername("vet1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret")).thenReturn("encoded");
        when(vetClient.createVet(any())).thenThrow(new RuntimeException("timeout"));
        when(vetClient.getVetByUsername("vet1")).thenReturn(new Object());

        assertEquals("Veterinarian account created successfully", authService.registerVet(request));
        verify(userRepository, never()).deleteByUsername("vet1");
    }

    @Test
    void loginReturnsJwtWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setUsername("  manu ");
        request.setPassword("secret");

        User user = new User();
        user.setUsername("manu");
        user.setPassword("$2a$10$encoded-secret");
        user.setRole("CUSTOMER");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "$2a$10$encoded-secret")).thenReturn(true);
        when(jwtUtil.generateToken("manu", "CUSTOMER")).thenReturn("jwt-token");

        String token = authService.login(request);

        assertEquals("jwt-token", token);
    }

    @Test
    void loginRejectsInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("manu");
        request.setPassword("wrong");

        User user = new User();
        user.setUsername("manu");
        user.setPassword("$2a$10$encoded-secret");
        user.setRole("CUSTOMER");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$encoded-secret")).thenReturn(false);

        ResponseStatusException exception =
                assertThrows(ResponseStatusException.class, () -> authService.login(request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Invalid credentials"));
    }

    @Test
    void loginRejectsMissingUser() {
        LoginRequest request = new LoginRequest();
        request.setUsername("missing");
        request.setPassword("secret");

        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(ResponseStatusException.class, () -> authService.login(request)).getStatusCode());
    }

    @Test
    void loginUpgradesPlainTextPasswordAfterSuccessfulMatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testadmin");
        request.setPassword("passadmin");

        User user = new User();
        user.setUsername("testadmin");
        user.setPassword("passadmin");
        user.setRole("ADMIN");

        when(userRepository.findByUsername("testadmin")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("passadmin")).thenReturn("$2a$10$hashed-admin");
        when(jwtUtil.generateToken("testadmin", "ADMIN")).thenReturn("admin-jwt");

        String token = authService.login(request);

        assertEquals("admin-jwt", token);
        verify(userRepository).save(user);
        assertEquals("$2a$10$hashed-admin", user.getPassword());
    }

    @Test
    void changePasswordUpdatesWhenCurrentPasswordMatches() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("new");

        User user = new User();
        user.setUsername("manu");
        user.setPassword("$2a$10$old");
        user.setRole("CUSTOMER");

        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("manu");
        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "$2a$10$old")).thenReturn(true);
        when(passwordEncoder.matches("new", "$2a$10$old")).thenReturn(false);
        when(passwordEncoder.encode("new")).thenReturn("$2a$10$new");

        assertEquals("Password updated successfully", authService.changePassword("Bearer token", request));
        assertEquals("$2a$10$new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsMissingBearerToken() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(ResponseStatusException.class, () -> authService.changePassword(null, request)).getStatusCode());
    }

    @Test
    void changePasswordRejectsInvalidToken() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        when(jwtUtil.validateToken("bad")).thenReturn(false);

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(ResponseStatusException.class, () -> authService.changePassword("Bearer bad", request)).getStatusCode());
    }

    @Test
    void changePasswordRejectsSameNewPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("old");
        request.setNewPassword("old");

        User user = new User();
        user.setUsername("manu");
        user.setPassword("$2a$10$old");

        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("manu");
        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old", "$2a$10$old")).thenReturn(true);

        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> authService.changePassword("Bearer token", request)).getStatusCode());
    }

    @Test
    void changePasswordRejectsUnknownUser() {
        ChangePasswordRequest request = new ChangePasswordRequest();

        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("missing");
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(ResponseStatusException.class, () -> authService.changePassword("Bearer token", request)).getStatusCode());
    }

    @Test
    void changePasswordRejectsWrongCurrentPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("new");

        User user = new User();
        user.setUsername("manu");
        user.setPassword("$2a$10$old");

        when(jwtUtil.validateToken("token")).thenReturn(true);
        when(jwtUtil.extractUsername("token")).thenReturn("manu");
        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "$2a$10$old")).thenReturn(false);

        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> authService.changePassword("Bearer token", request)).getStatusCode());
    }

    @Test
    void loginRejectsNullStoredPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("manu");
        request.setPassword("secret");

        User user = new User();
        user.setUsername("manu");
        user.setPassword(null);

        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(ResponseStatusException.class, () -> authService.login(request)).getStatusCode());
    }

    @Test
    void loginRejectsPlainTextPasswordMismatch() {
        LoginRequest request = new LoginRequest();
        request.setUsername("manu");
        request.setPassword("wrong");

        User user = new User();
        user.setUsername("manu");
        user.setPassword("secret");

        when(userRepository.findByUsername("manu")).thenReturn(Optional.of(user));

        assertEquals(HttpStatus.UNAUTHORIZED,
                assertThrows(ResponseStatusException.class, () -> authService.login(request)).getStatusCode());
        verify(userRepository, never()).save(user);
    }

    private VetRegisterRequest vetRegisterRequest() {
        VetRegisterRequest request = new VetRegisterRequest();
        request.setUsername("vet1");
        request.setPassword("secret");
        request.setName("Dr Vet");
        request.setPhone("9876543210");
        request.setEmail("vet@example.com");
        request.setSpecialization("Surgery");
        request.setExperience(6);
        request.setClinicAddress("Chennai");
        request.setAvailableDays("Mon");
        request.setAvailableTime("10 AM - 1 PM");
        request.setConsultationFee(java.math.BigDecimal.valueOf(500));
        return request;
    }
}
