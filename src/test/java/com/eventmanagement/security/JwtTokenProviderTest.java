// src/test/java/com/eventmanagement/security/JwtTokenProviderTest.java
package com.eventmanagement.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {

    @InjectMocks
    private JwtTokenProvider jwtTokenProvider;

    private final String testSecret = "Bau8dsS/SGos+SxfI9Dkg5CGFepS6R/SLDkWCoblk9s=";
    private final long testExpiration = 86400000;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", testExpiration);
    }

    @Test
    void whenGenerateToken_withAuthentication_thenReturnValidToken() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test@example.com");

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void whenGenerateToken_withUserPrincipal_thenReturnValidToken() {
        Authentication authentication = mock(Authentication.class);
        UserPrincipal userPrincipal = mock(UserPrincipal.class);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(userPrincipal.getId()).thenReturn(java.util.UUID.randomUUID());

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    void whenGetUserIdFromToken_withValidToken_thenReturnUserId() {
        String userId = "test@example.com";
        String token = createTestToken(userId);

        String extractedUserId = jwtTokenProvider.getUserIdFromToken(token);

        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    void whenValidateToken_withValidToken_thenReturnTrue() {
        String token = createTestToken("test@example.com");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void whenValidateToken_withInvalidToken_thenReturnFalse() {
        String invalidToken = "invalid.token.here";

        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void whenValidateToken_withExpiredToken_thenReturnFalse() {
        String expiredToken = createExpiredTestToken();

        boolean isValid = jwtTokenProvider.validateToken(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void whenGetExpirationFromToken_withValidToken_thenReturnExpirationTime() {
        String token = createTestToken("test@example.com");

        long expiration = jwtTokenProvider.getExpirationFromToken(token);

        assertThat(expiration).isGreaterThan(System.currentTimeMillis());
    }

    private String createTestToken(String subject) {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Date expiryDate = new Date(System.currentTimeMillis() + testExpiration);

        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .signWith(key)
                .compact();
    }

    private String createExpiredTestToken() {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes());
        Date expiredDate = new Date(System.currentTimeMillis() - 1000);

        return Jwts.builder()
                .setSubject("test@example.com")
                .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
                .setExpiration(expiredDate)
                .signWith(key)
                .compact();
    }
}
