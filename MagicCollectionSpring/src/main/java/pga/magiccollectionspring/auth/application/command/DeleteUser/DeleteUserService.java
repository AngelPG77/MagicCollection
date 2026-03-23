package pga.magiccollectionspring.auth.application.command.DeleteUser;

import pga.magiccollectionspring.shared.abstractions.ICommandService;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class DeleteUserService implements ICommandService<DeleteUserCommand, DeleteUserResponse> {

    private final IUserRepository userRepository;

    public DeleteUserService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public DeleteUserResponse execute(DeleteUserCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", command.userId().toString()));

        userRepository.deleteById(user.getId());

        return new DeleteUserResponse(true, "Usuario eliminado con exito");
    }
}
