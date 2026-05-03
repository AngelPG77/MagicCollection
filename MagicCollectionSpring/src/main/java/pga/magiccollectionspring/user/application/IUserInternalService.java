package pga.magiccollectionspring.user.application;

import pga.magiccollectionspring.user.domain.User;
import java.util.Optional;

public interface IUserInternalService {
    Optional<User> findByUsername(String username);
}
