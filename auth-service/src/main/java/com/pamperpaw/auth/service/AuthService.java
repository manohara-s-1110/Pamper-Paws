package com.pamperpaw.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pamperpaw.auth.client.CustomerClient;
import com.pamperpaw.auth.client.VetClient;
import com.pamperpaw.auth.dto.ChangePasswordRequest;
import com.pamperpaw.auth.dto.CustomerProfileRequest;
import com.pamperpaw.auth.dto.LoginRequest;
import com.pamperpaw.auth.dto.RegisterRequest;
import com.pamperpaw.auth.dto.VetProfileRequest;
import com.pamperpaw.auth.dto.VetRegisterRequest;
import com.pamperpaw.auth.entity.User;
import com.pamperpaw.auth.repository.UserRepository;
import com.pamperpaw.auth.util.JwtUtil;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CustomerClient customerClient;
    private final VetClient vetClient;
    private final ObjectMapper objectMapper;

    public String register(RegisterRequest request) {
        String username = request.getUsername().trim();
        String role = normalizeRole(request.getRole());

        if ("ADMIN".equals(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be created from the public registration endpoint");
        }

        if ("VET".equals(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use the veterinarian registration endpoint for vet accounts");
        }

        validateCustomerProfile(request);
        createAuthUser(username, request.getPassword(), role);

        try {
            customerClient.createCustomer(toCustomerProfileRequest(request, username));
            log.info("Registered customer across auth and customer services username={}", username);
            return "User registered successfully";
        } catch (Exception ex) {
            if (customerProfileExists(username)) {
                log.warn("Customer profile already exists after downstream exception for username={}", username, ex);
                return "User registered successfully";
            }

            userRepository.deleteByUsername(username);
            log.error("Rolled back auth registration for customer username={}", username, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    extractServiceError(ex, "Customer profile creation failed and registration was rolled back")
            );
        }
    }

    public String registerVet(VetRegisterRequest request) {
        String username = request.getUsername().trim();
        createAuthUser(username, request.getPassword(), "VET");

        try {
            vetClient.createVet(toVetProfileRequest(request, username));
            log.info("Registered veterinarian across auth and vet services username={}", username);
            return "Veterinarian account created successfully";
        } catch (Exception ex) {
            if (vetProfileExists(username)) {
                log.warn("Vet profile already exists after downstream exception for username={}", username, ex);
                return "Veterinarian account created successfully";
            }

            userRepository.deleteByUsername(username);
            log.error("Rolled back auth registration for veterinarian username={}", username, ex);
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    extractServiceError(ex, "Veterinarian profile creation failed and registration was rolled back")
            );
        }
    }

    public String login(LoginRequest request) {
        String username = request.getUsername().trim();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!matchesPassword(user, request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("Generated JWT for username={} role={}", username, user.getRole());
        return jwtUtil.generateToken(user.getUsername(), user.getRole());
    }

    public String changePassword(String authorization, ChangePasswordRequest request) {
        String token = extractBearerToken(authorization);
        if (!jwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        String username = jwtUtil.extractUsername(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!matchesPassword(user, request.getCurrentPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Updated password for username={}", username);
        return "Password updated successfully";
    }

    private void createAuthUser(String username, String password, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        userRepository.save(user);
    }

    private String normalizeRole(String role) {
        String normalizedRole = role.trim().toUpperCase();
        if (!normalizedRole.equals("ADMIN") && !normalizedRole.equals("CUSTOMER") && !normalizedRole.equals("VET")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be ADMIN, CUSTOMER, or VET");
        }
        return normalizedRole;
    }

    private VetProfileRequest toVetProfileRequest(VetRegisterRequest request, String username) {
        VetProfileRequest profileRequest = new VetProfileRequest();
        profileRequest.setUsername(username);
        profileRequest.setName(request.getName().trim());
        profileRequest.setPhone(request.getPhone().trim());
        profileRequest.setEmail(request.getEmail().trim());
        profileRequest.setSpecialization(request.getSpecialization().trim());
        profileRequest.setExperience(request.getExperience());
        profileRequest.setClinicAddress(request.getClinicAddress().trim());
        profileRequest.setAvailableDays(request.getAvailableDays().trim());
        profileRequest.setAvailableTime(request.getAvailableTime().trim());
        profileRequest.setConsultationFee(request.getConsultationFee());
        return profileRequest;
    }

    private CustomerProfileRequest toCustomerProfileRequest(RegisterRequest request, String username) {
        CustomerProfileRequest profileRequest = new CustomerProfileRequest();
        profileRequest.setUsername(username);
        profileRequest.setName(request.getName().trim());
        profileRequest.setPhone(request.getPhone().trim());
        profileRequest.setEmail(request.getEmail().trim());
        profileRequest.setAddress(request.getAddress().trim());
        return profileRequest;
    }

    private void validateCustomerProfile(RegisterRequest request) {
        if (isBlank(request.getEmail()) || isBlank(request.getPhone()) || isBlank(request.getAddress())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email, phone, and address are required for customer registration");
        }
    }

    private boolean vetProfileExists(String username) {
        try {
            vetClient.getVetByUsername(username);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean customerProfileExists(String username) {
        try {
            customerClient.getCustomerByUsername(username);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private String extractServiceError(Exception exception, String fallbackMessage) {
        if (exception instanceof FeignException feignException && feignException.contentUTF8() != null && !feignException.contentUTF8().isBlank()) {
            try {
                Map<String, Object> payload = objectMapper.readValue(feignException.contentUTF8(), new TypeReference<>() {});
                Object message = payload.get("message");
                if (message instanceof String serviceMessage && !serviceMessage.isBlank()) {
                    return serviceMessage;
                }
            } catch (Exception ignored) {
                log.debug("Unable to parse downstream service error body");
            }
        }

        return fallbackMessage;
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization token is required");
        }
        return authorization.substring(7);
    }

    private boolean matchesPassword(User user, String rawPassword) {
        String storedPassword = user.getPassword();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!rawPassword.equals(storedPassword)) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
        log.info("Upgraded plain-text password to BCrypt for username={}", user.getUsername());
        return true;
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
