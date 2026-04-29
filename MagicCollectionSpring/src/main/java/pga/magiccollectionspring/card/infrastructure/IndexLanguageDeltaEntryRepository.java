package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.IIndexLanguageDeltaEntryRepository;
import pga.magiccollectionspring.card.domain.IndexLanguageDeltaEntry;

import java.util.List;

@Repository
public interface IndexLanguageDeltaEntryRepository extends JpaRepository<IndexLanguageDeltaEntry, Long>, IIndexLanguageDeltaEntryRepository {
    List<IndexLanguageDeltaEntry> findByLanguageCodeAndTargetVersionLongGreaterThanAndTargetVersionLongLessThanEqualOrderByTargetVersionLongAscIdAsc(
            String languageCode,
            long sinceVersion,
            long targetVersion
    );
}
