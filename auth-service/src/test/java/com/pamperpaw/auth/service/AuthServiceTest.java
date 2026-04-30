package com.pamperpaw.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.auth.client.CustomerClient;
import com.pamperpaw.auth.client.VetClient;
import com.pamperpaw.auth.dto.LoginRequest;
import com.pamperpaw.auth.dto.RegisterRequest;
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
}
