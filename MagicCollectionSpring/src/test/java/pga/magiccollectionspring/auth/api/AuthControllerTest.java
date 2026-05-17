package pga.magiccollectionspring.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import pga.magiccollectionspring.auth.application.command.DeleteUser.DeleteUserService;
import pga.magiccollectionspring.auth.application.command.Register.RegisterResponse;
import pga.magiccollectionspring.auth.application.command.Register.RegisterService;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordService;
import pga.magiccollectionspring.auth.application.command.UpdateUser.UpdateUserService;
import pga.magiccollectionspring.auth.application.query.Login.LoginQuery;
import pga.magiccollectionspring.auth.application.query.Login.LoginResponse;
import pga.magiccollectionspring.auth.application.query.Login.LoginService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.GlobalExceptionHandler;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.shared.security.JwtService;
import pga.magiccollectionspring.shared.security.RefreshTokenService;
import pga.magiccollectionspring.user.domain.IUserRepository;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockitoBean RegisterService registerService;
    @MockitoBean LoginService loginService;
    @MockitoBean UpdateUserService updateUserService;
    @MockitoBean UpdatePasswordService updatePasswordService;
    @MockitoBean DeleteUserService deleteUserService;
    @MockitoBean IUserRepository userRepository;
    @MockitoBean CurrentUserProvider currentUserProvider;
    @MockitoBean RefreshTokenService refreshTokenService;
    @MockitoBean JwtService jwtService;

    @Test
    void login_returns200WithToken_whenCredentialsValid() throws Exception {
        when(loginService.execute(any(LoginQuery.class)))
                .thenReturn(new LoginResponse("access.token", null, 1L));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "pass", "rememberMe", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access.token"));
    }

    @Test
    void login_returns401_whenCredentialsInvalid() throws Exception {
        when(loginService.execute(any(LoginQuery.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "alice", "password", "wrong", "rememberMe", false))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_returns200_whenUsernameNew() throws Exception {
        when(registerService.execute(any()))
                .thenReturn(new RegisterResponse("Usuario registrado con exito."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "newuser", "password", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void register_returns409_whenUsernameAlreadyExists() throws Exception {
        when(registerService.execute(any()))
                .thenThrow(new ConflictException("El nombre de usuario ya existe: newuser"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "newuser", "password", "secret123"))))
                .andExpect(status().isConflict());
    }
}
