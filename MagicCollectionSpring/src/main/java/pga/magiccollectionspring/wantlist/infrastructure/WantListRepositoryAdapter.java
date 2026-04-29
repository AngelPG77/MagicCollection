package pga.magiccollectionspring.wantlist.infrastructure;

import pga.magiccollectionspring.wantlist.domain.IWantListRepository;
import pga.magiccollectionspring.wantlist.domain.WantList;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class WantListRepositoryAdapter implements IWantListRepository {

    private final JpaWantListRepository jpaRepository;

    public WantListRepositoryAdapter(JpaWantListRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WantList save(WantList wantList) {
        return jpaRepository.save(wantList);
    }

    @Override
    public Optional<WantList> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<WantList> findByOwnerId(Long userId) {
        return jpaRepository.findByOwnerId(userId);
    }

    @Override
    public Optional<WantList> findByNameAndOwnerId(String name, Long userId) {
        return jpaRepository.findByNameAndOwnerId(name, userId);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNameAndOwnerId(String name, Long userId) {
        return jpaRepository.existsByNameAndOwnerId(name, userId);
    }
}
