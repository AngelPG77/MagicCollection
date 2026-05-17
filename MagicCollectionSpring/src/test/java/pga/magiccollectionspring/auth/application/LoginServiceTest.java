package pga.magiccollectionspring.auth.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import pga.magiccollectionspring.auth.application.query.Login.LoginQuery;
import pga.magiccollectionspring.auth.application.query.Login.LoginResponse;
import pga.magiccollectionspring.auth.application.query.Login.LoginService;
import pga.magiccollectionspring.shared.security.JwtService;
import pga.magiccollectionspring.shared.security.RefreshTokenService;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.RefreshToken;
import pga.magiccollectionspring.user.domain.User;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private IUserRepository userRepository;

    private LoginService loginService;

    @BeforeEach
    void setUp() {
        loginService = new LoginService(authenticationManager, jwtService, refreshTokenService, userRepository);
    }

    @Test
    void execute_returnsToken_withoutRefreshToken_whenRememberMeFalse() {
        User user = new User(1L, "alice", "hashed");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("alice")).thenReturn("access.token.here");

        LoginResponse response = loginService.execute(new LoginQuery("alice", "pass", false));

        assertThat(response.token()).isEqualTo("access.token.here");
        assertThat(response.refreshToken()).isNull();
        verify(refreshTokenService, never()).createRefreshToken(any());
    }

    @Test
    void execute_returnsBothTokens_whenRememberMeTrue() {
        User user = new User(1L, "alice", "hashed");
        RefreshToken rt = new RefreshToken();
        rt.setToken("refresh.token.here");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("alice")).thenReturn("access.token.here");
        when(refreshTokenService.createRefreshToken(1L)).thenReturn(rt);

        LoginResponse response = loginService.execute(new LoginQuery("alice", "pass", true));

        assertThat(response.token()).isEqualTo("access.token.here");
        assertThat(response.refreshToken()).isEqualTo("refresh.token.here");
        verify(refreshTokenService).createRefreshToken(1L);
    }

    @Test
    void execute_throwsBadCredentials_whenPasswordWrong() {
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> loginService.execute(new LoginQuery("alice", "wrong", false)))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
