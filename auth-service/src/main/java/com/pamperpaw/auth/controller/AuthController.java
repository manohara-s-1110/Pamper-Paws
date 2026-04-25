package com.pamperpaw.auth.controller;

import com.pamperpaw.auth.dto.LoginRequest;
import com.pamperpaw.auth.dto.RegisterRequest;
import com.pamperpaw.auth.dto.ChangePasswordRequest;
import com.pamperpaw.auth.dto.VetRegisterRequest;
import com.pamperpaw.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public String register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for username={}", request.getUsername());
        return authService.register(request);
    }

    @PostMapping("/register/vet")
    public String registerVet(@Valid @RequestBody VetRegisterRequest request) {
        log.info("Received veterinarian registration request for username={}", request.getUsername());
        return authService.registerVet(request);
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for username={}", request.getUsername());
        return authService.login(request);
    }

    @PostMapping(value = "/login", params = {"username", "password"})
    public String loginWithParams(@RequestParam String username, @RequestParam String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        log.info("Received param-based login request for username={}", username);
        return authService.login(request);
    }

    @PutMapping("/password")
    public String changePassword(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization,
                                 @Valid @RequestBody ChangePasswordRequest request) {
        return authService.changePassword(authorization, request);
    }
}
