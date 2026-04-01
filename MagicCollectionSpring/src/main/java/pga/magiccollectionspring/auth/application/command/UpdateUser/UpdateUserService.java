package pga.magiccollectionspring.auth.application.command.UpdateUser;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ConflictException;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.shared.security.JwtService;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UpdateUserService implements ICommandService<UpdateUserCommand, UpdateUserResponse> {

    private final IUserRepository userRepository;
    private final JwtService jwtService;

    public UpdateUserService(IUserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public UpdateUserResponse execute(UpdateUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", command.userId().toString()));

        if (!user.getUsername().equals(command.newUsername()) && 
            userRepository.existsByUsername(command.newUsername())) {
            throw new ConflictException("El nombre de usuario ya existe: " + command.newUsername());
        }

        user.setUsername(command.newUsername());
        userRepository.save(user);

        // Generar nuevo token con el nuevo username
        String newToken = jwtService.generateToken(command.newUsername());

        return new UpdateUserResponse(true, "Nombre de usuario actualizado con exito", command.newUsername(), newToken);
    }
}
