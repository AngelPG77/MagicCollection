package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;

public interface IIndexLanguageDeltaEntryRepository extends IRepository<IndexLanguageDeltaEntry, Long> {
    List<IndexLanguageDeltaEntry> findByLanguageCodeAndTargetVersionLongGreaterThanAndTargetVersionLongLessThanEqualOrderByTargetVersionLongAscIdAsc(
            String languageCode,
            long sinceVersion,
            long targetVersion
    );
}
