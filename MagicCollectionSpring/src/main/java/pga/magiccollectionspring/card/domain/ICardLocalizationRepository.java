package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface ICardLocalizationRepository extends IRepository<CardLocalization, CardLocalizationId> {
    List<CardLocalization> findByLanguageCodeAndOracleIds(String languageCode, Set<String> oracleIds);

    LocalDateTime findMaxLastUpdated();

    LocalDateTime findMaxLastUpdatedByLanguage(String languageCode);

    Set<String> findOracleIdsUpdatedSince(String languageCode, LocalDateTime since);

    List<CardLocalization> findAllByIds(Iterable<CardLocalizationId> ids);
}
