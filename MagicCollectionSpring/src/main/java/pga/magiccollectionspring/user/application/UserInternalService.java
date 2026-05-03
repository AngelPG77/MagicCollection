package pga.magiccollectionspring.user.application;

import org.springframework.stereotype.Service;
import pga.magiccollectionspring.user.domain.IUserRepository;
import pga.magiccollectionspring.user.domain.User;
import java.util.Optional;

@Service
class UserInternalService implements IUserInternalService {

    private final IUserRepository userRepository;

    public UserInternalService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
