package pga.magiccollectionspring.wantlist.infrastructure;

import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JpaWantListRepository extends JpaRepository<WantList, Long> {
    List<WantList> findByOwnerId(Long userId);
    Optional<WantList> findByNameAndOwnerId(String name, Long userId);
    boolean existsByNameAndOwnerId(String name, Long userId);
}
