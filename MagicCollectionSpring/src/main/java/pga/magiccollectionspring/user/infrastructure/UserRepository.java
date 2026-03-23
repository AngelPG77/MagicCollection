package pga.magiccollectionspring.user.infrastructure;

import pga.magiccollectionspring.user.domain.User;
import pga.magiccollectionspring.user.domain.IUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, IUserRepository {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}