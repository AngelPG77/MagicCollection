package pga.magiccollectionspring.collection.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;
import pga.magiccollectionspring.user.domain.User;

import java.util.List;

public interface ICollectionRepository extends IRepository<Collection, Long> {
    List<Collection> findByOwner_Username(String username);
    boolean existsByNameAndOwner(String name, User owner);
}