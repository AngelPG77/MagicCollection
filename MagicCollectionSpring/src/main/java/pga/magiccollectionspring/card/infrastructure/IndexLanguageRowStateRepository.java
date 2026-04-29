package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.IIndexLanguageRowStateRepository;
import pga.magiccollectionspring.card.domain.IndexLanguageRowState;
import pga.magiccollectionspring.card.domain.IndexLanguageRowStateId;

import java.util.List;

@Repository
public interface IndexLanguageRowStateRepository extends JpaRepository<IndexLanguageRowState, IndexLanguageRowStateId>, IIndexLanguageRowStateRepository {
    List<IndexLanguageRowState> findByIdLanguageCode(String languageCode);
}
