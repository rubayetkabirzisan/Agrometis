package com.rubaet.agri.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JwtService}.
 * Uses reflection to inject a test secret key without Spring context.
 */
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails testUser;

    // Valid base64-encoded 256-bit key for HS256
    private static final String TEST_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        // Inject the secret key via reflection (avoiding Spring context)
        Field secretKeyField = JwtService.class.getDeclaredField("secretKey");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtService, TEST_SECRET);

        testUser = new User("farmer@example.com", "password", Collections.emptyList());
    }

    @Test
    @DisplayName("Generated access token contains correct subject (email)")
    void accessTokenContainsCorrectSubject() {
        String token = jwtService.generateToken(testUser);
        assertNotNull(token);
        assertEquals("farmer@example.com", jwtService.extractUsername(token));
    }

    @Test
    @DisplayName("Generated access token is valid for the same user")
    void accessTokenIsValidForSameUser() {
        String token = jwtService.generateToken(testUser);
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    @DisplayName("Access token is invalid for a different user")
    void accessTokenIsInvalidForDifferentUser() {
        String token = jwtService.generateToken(testUser);
        UserDetails otherUser = new User("other@example.com", "pass", Collections.emptyList());
        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    @DisplayName("Refresh token has type=refresh claim")
    void refreshTokenHasCorrectType() {
        String refreshToken = jwtService.generateRefreshToken(testUser);
        assertNotNull(refreshToken);
        assertTrue(jwtService.isRefreshToken(refreshToken));
    }

    @Test
    @DisplayName("Access token is NOT a refresh token")
    void accessTokenIsNotRefreshToken() {
        String accessToken = jwtService.generateToken(testUser);
        assertFalse(jwtService.isRefreshToken(accessToken));
    }
}
