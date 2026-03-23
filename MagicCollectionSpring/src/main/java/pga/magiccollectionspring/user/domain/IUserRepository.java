package pga.magiccollectionspring.user.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.Optional;

public interface IUserRepository extends IRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}