package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.IIndexLanguageStateRepository;
import pga.magiccollectionspring.card.domain.IndexLanguageState;

import java.util.Optional;

@Repository
public interface IndexLanguageStateRepository extends JpaRepository<IndexLanguageState, String>, IIndexLanguageStateRepository {
    Optional<IndexLanguageState> findByLanguageCode(String languageCode);
}
