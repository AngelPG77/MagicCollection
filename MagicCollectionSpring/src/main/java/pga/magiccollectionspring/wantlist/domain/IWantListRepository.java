package pga.magiccollectionspring.wantlist.domain;

import java.util.List;
import java.util.Optional;

public interface IWantListRepository {
    WantList save(WantList wantList);
    Optional<WantList> findById(Long id);
    List<WantList> findByOwnerId(Long userId);
    Optional<WantList> findByNameAndOwnerId(String name, Long userId);
    void deleteById(Long id);
    boolean existsByNameAndOwnerId(String name, Long userId);
}
