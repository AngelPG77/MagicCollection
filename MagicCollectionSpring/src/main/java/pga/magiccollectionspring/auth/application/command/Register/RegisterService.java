package pga.magiccollectionspring.auth.application.command.Register;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegisterService implements ICommandService<RegisterCommand, RegisterResponse> {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterResponse execute(RegisterCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new ConflictException("El nombre de usuario ya existe: " + command.username());
        }
        User user = new User();
        user.setUsername(command.username());
        user.setPassword(passwordEncoder.encode(command.password()));
        userRepository.save(user);
        return new RegisterResponse("Usuario registrado con exito. Ahora puedes hacer login.");
    }
}