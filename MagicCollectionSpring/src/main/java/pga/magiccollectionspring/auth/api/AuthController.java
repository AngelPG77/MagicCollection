package pga.magiccollectionspring.auth.api;

import pga.magiccollectionspring.auth.api.dto.LoginRequest;
import pga.magiccollectionspring.auth.api.dto.RegisterRequest;
import pga.magiccollectionspring.auth.api.dto.UpdatePasswordRequest;
import pga.magiccollectionspring.auth.api.dto.UpdateUserRequest;
import pga.magiccollectionspring.auth.application.command.DeleteUser.DeleteUserCommand;
import pga.magiccollectionspring.auth.application.command.DeleteUser.DeleteUserResponse;
import pga.magiccollectionspring.auth.application.command.DeleteUser.DeleteUserService;
import pga.magiccollectionspring.auth.application.command.Register.RegisterCommand;
import pga.magiccollectionspring.auth.application.command.Register.RegisterResponse;
import pga.magiccollectionspring.auth.application.command.Register.RegisterService;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordCommand;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordResponse;
import pga.magiccollectionspring.auth.application.command.UpdatePassword.UpdatePasswordService;
import pga.magiccollectionspring.auth.application.command.UpdateUser.UpdateUserCommand;
import pga.magiccollectionspring.auth.application.command.UpdateUser.UpdateUserResponse;
import pga.magiccollectionspring.auth.application.command.UpdateUser.UpdateUserService;
import pga.magiccollectionspring.auth.application.query.Login.LoginQuery;
import pga.magiccollectionspring.auth.application.query.Login.LoginResponse;
import pga.magiccollectionspring.auth.application.query.Login.LoginService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.CurrentUserProvider;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.shared.security.JwtService;
import pga.magiccollectionspring.shared.security.RefreshTokenService;
import pga.magiccollectionspring.user.domain.RefreshToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.util.Map;

/**
 * Controller for authentication and user management.
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints for registration, login, and user account management")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final UpdateUserService updateUserService;
    private final UpdatePasswordService updatePasswordService;
    private final DeleteUserService deleteUserService;
    private final IUserRepository userRepository;
    private final CurrentUserProvider currentUserProvider;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;

    public AuthController(RegisterService registerService, LoginService loginService, 
                          UpdateUserService updateUserService, UpdatePasswordService updatePasswordService,
                          DeleteUserService deleteUserService, IUserRepository userRepository,
                          CurrentUserProvider currentUserProvider, RefreshTokenService refreshTokenService,
                          JwtService jwtService) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.updateUserService = updateUserService;
        this.updatePasswordService = updatePasswordService;
        this.deleteUserService = deleteUserService;
        this.userRepository = userRepository;
        this.currentUserProvider = currentUserProvider;
        this.refreshTokenService = refreshTokenService;
        this.jwtService = jwtService;
    }

    /**
     * Refreshes the JWT access token.
     * @param request Map containing the refreshToken.
     * @return New access token.
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh JWT token", description = "Generates a new access token from a valid refresh token")
    @ApiResponse(responseCode = "200", description = "Token refreshed successfully")
    @ApiResponse(responseCode = "403", description = "Invalid or expired refresh token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        
        return refreshTokenService.findByToken(refreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String token = jwtService.generateToken(user.getUsername());
                    return ResponseEntity.ok(Map.of(
                            "token", token,
                            "refreshToken", refreshToken
                    ));
                })
                .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
    }

    /**
     * Registers a new user.
     * @param request Registration data.
     * @return Registration response.
     */
    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Creates a new user account in the system")
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = registerService.execute(new RegisterCommand(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(response);
    }

    /**
     * Logs into the system.
     * @param request Access credentials.
     * @return JWT token and user information.
     */
    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT token")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Incorrect credentials")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.execute(new LoginQuery(request.getUsername(), request.getPassword(), request.isRememberMe()));
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the username.
     * @param request New username.
     * @return Update response.
     */
    @PutMapping("/update-username")
    @Operation(summary = "Update username", description = "Changes the username of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Username updated")
    public ResponseEntity<UpdateUserResponse> updateUsername(@RequestBody UpdateUserRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        UpdateUserResponse response = updateUserService.execute(
                new UpdateUserCommand(user.getId(), request.getNewUsername()));
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the user password.
     * @param request Current and new passwords.
     * @return Update response.
     */
    @PutMapping("/update-password")
    @Operation(summary = "Update password", description = "Changes the password of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Password updated")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestBody UpdatePasswordRequest request) {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        UpdatePasswordResponse response = updatePasswordService.execute(
                new UpdatePasswordCommand(user.getId(), request.getCurrentPassword(), request.getNewPassword()));
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes the current user account.
     * @return Deletion response.
     */
    @DeleteMapping("/delete")
    @Operation(summary = "Delete account", description = "Deletes the account of the authenticated user")
    @ApiResponse(responseCode = "200", description = "Account deleted successfully")
    public ResponseEntity<DeleteUserResponse> deleteUser() {
        String username = currentUserProvider.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", username));
        
        DeleteUserResponse response = deleteUserService.execute(new DeleteUserCommand(user.getId()));
        return ResponseEntity.ok(response);
    }
}
