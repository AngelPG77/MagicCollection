package pga.magiccollectionspring.collection.infrastructure;

import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.collection.domain.ICollectionRepository;
import pga.magiccollectionspring.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long>, ICollectionRepository {
    List<Collection> findByOwner_Username(String username);
    boolean existsByNameAndOwner(String name, User owner);
}