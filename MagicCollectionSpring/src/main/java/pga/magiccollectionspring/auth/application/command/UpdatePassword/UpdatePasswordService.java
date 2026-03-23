package pga.magiccollectionspring.auth.application.command.UpdatePassword;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.exception.UnauthorizedException;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UpdatePasswordService implements ICommandService<UpdatePasswordCommand, UpdatePasswordResponse> {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdatePasswordService(IUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public UpdatePasswordResponse execute(UpdatePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", command.userId().toString()));

        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            throw new UnauthorizedException("La contraseña actual es incorrecta");
        }

        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        return new UpdatePasswordResponse(true, "Contraseña actualizada con exito");
    }
}
