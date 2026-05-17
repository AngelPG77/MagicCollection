package pga.magiccollectionspring.shared.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-characters-long!";
    private static final long EXPIRATION_MS = 86_400_000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MS);
    }

    @Test
    void generateToken_returnsTokenContainingUsername() {
        String token = jwtService.generateToken("alice");

        assertThat(jwtService.extractUsername(token)).isEqualTo("alice");
    }

    @Test
    void isTokenValid_returnsTrue_forFreshTokenAndMatchingUser() {
        String token = jwtService.generateToken("alice");
        UserDetails userDetails = buildUserDetails("alice");

        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    void isTokenValid_throwsException_forExpiredToken() {
        // jjwt throws ExpiredJwtException (a JwtException) when parsing an expired token;
        // isTokenValid propagates it rather than returning false.
        JwtService expiredService = new JwtService(SECRET, -1L);
        String expiredToken = expiredService.generateToken("alice");
        UserDetails userDetails = buildUserDetails("alice");

        assertThatThrownBy(() -> jwtService.isTokenValid(expiredToken, userDetails))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void isTokenValid_returnsFalse_forDifferentUser() {
        String token = jwtService.generateToken("alice");
        UserDetails bob = buildUserDetails("bob");

        assertThat(jwtService.isTokenValid(token, bob)).isFalse();
    }

    @Test
    void extractUsername_throwsException_forTamperedToken() {
        String token = jwtService.generateToken("alice");
        // Swap the payload section with a base64-encoded tampered value
        String[] parts = token.split("\\.");
        String tamperedToken = parts[0] + ".dGFtcGVyZWQ." + parts[2];

        assertThatThrownBy(() -> jwtService.extractUsername(tamperedToken))
                .isInstanceOf(JwtException.class);
    }

    private UserDetails buildUserDetails(String username) {
        return User.withUsername(username)
                .password("irrelevant")
                .authorities(Collections.emptyList())
                .build();
    }
}
