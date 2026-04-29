package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.MtgSet;
import pga.magiccollectionspring.card.domain.IMtgSetRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MtgSetRepository extends JpaRepository<MtgSet, Long>, IMtgSetRepository {
    @Override
    Optional<MtgSet> findByCode(String code);
    
    @Override
    List<MtgSet> findAllByOrderByReleaseDateDesc();
}
