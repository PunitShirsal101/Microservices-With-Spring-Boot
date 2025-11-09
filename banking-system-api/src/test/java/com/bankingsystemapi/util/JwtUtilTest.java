package com.bankingsystemapi.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtUtilTest {

    private JwtUtil jwtUtil = new JwtUtil();

    @Test
    public void testGenerateToken() {
        String token = jwtUtil.generateToken("testuser");

        assertTrue(token.length() > 0);
    }

    @Test
    public void testExtractUsername() {
        String token = jwtUtil.generateToken("testuser");

        String username = jwtUtil.extractUsername(token);

        assertEquals("testuser", username);
    }

    @Test
    public void testValidateToken_Valid() {
        String token = jwtUtil.generateToken("testuser");

        boolean isValid = jwtUtil.validateToken(token, "testuser");

        assertTrue(isValid);
    }

    @Test
    public void testValidateToken_InvalidUsername() {
        String token = jwtUtil.generateToken("testuser");

        boolean isValid = jwtUtil.validateToken(token, "wronguser");

        assertFalse(isValid);
    }
}
