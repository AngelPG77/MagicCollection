package pga.magiccollectionspring.collection.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICollectionCardRepository extends JpaRepository<CollectionCard, Long> {
    
    @Query("SELECT c FROM CollectionCard c JOIN FETCH c.collection WHERE c.collection.owner.id = :ownerId")
    List<CollectionCard> findByCollectionOwnerId(@Param("ownerId") Long ownerId);
}
