package com.pamperpaw.auth.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil("test-secret-key-for-auth-service-1234567890", 3_600_000L);

    @Test
    void generateAndValidateToken() {
        String token = jwtUtil.generateToken("manu", "ADMIN");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("manu", jwtUtil.extractUsername(token));
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void rejectInvalidToken() {
        assertFalse(jwtUtil.validateToken("invalid-token"));
    }
}
