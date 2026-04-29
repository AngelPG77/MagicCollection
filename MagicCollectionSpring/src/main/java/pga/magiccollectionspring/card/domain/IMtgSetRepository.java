package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;
import java.util.List;
import java.util.Optional;

public interface IMtgSetRepository extends IRepository<MtgSet, Long> {
    Optional<MtgSet> findByCode(String code);
    List<MtgSet> findAllByOrderByReleaseDateDesc();
}
