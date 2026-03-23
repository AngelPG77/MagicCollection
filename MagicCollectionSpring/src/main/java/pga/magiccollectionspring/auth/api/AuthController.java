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
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final RegisterService registerService;
    private final LoginService loginService;
    private final UpdateUserService updateUserService;
    private final UpdatePasswordService updatePasswordService;
    private final DeleteUserService deleteUserService;
    private final IUserRepository userRepository;

    public AuthController(RegisterService registerService, LoginService loginService, 
                          UpdateUserService updateUserService, UpdatePasswordService updatePasswordService,
                          DeleteUserService deleteUserService, IUserRepository userRepository) {
        this.registerService = registerService;
        this.loginService = loginService;
        this.updateUserService = updateUserService;
        this.updatePasswordService = updatePasswordService;
        this.deleteUserService = deleteUserService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        RegisterResponse response = registerService.execute(new RegisterCommand(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = loginService.execute(new LoginQuery(request.getUsername(), request.getPassword()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-username")
    public ResponseEntity<UpdateUserResponse> updateUsername(@RequestBody UpdateUserRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
        
        UpdateUserResponse response = updateUserService.execute(
                new UpdateUserCommand(user.getId(), request.getNewUsername()));
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-password")
    public ResponseEntity<UpdatePasswordResponse> updatePassword(@RequestBody UpdatePasswordRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
        
        UpdatePasswordResponse response = updatePasswordService.execute(
                new UpdatePasswordCommand(user.getId(), request.getCurrentPassword(), request.getNewPassword()));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<DeleteUserResponse> deleteUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", username));
        
        DeleteUserResponse response = deleteUserService.execute(new DeleteUserCommand(user.getId()));
        return ResponseEntity.ok(response);
    }
}