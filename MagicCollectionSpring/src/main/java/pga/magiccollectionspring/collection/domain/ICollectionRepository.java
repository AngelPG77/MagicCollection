package pga.magiccollectionspring.collection.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;
import pga.magiccollectionspring.user.domain.User;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ICollectionRepository extends IRepository<Collection, Long> {
    
    @Query("SELECT DISTINCT c FROM Collection c LEFT JOIN FETCH c.cards WHERE c.owner.username = :username")
    List<Collection> findByOwner_UsernameWithCards(@Param("username") String username);
    
    List<Collection> findByOwner_Username(String username);
    boolean existsByNameAndOwner(String name, User owner);
}