package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.Optional;

public interface IIndexLanguageStateRepository extends IRepository<IndexLanguageState, String> {
    Optional<IndexLanguageState> findByLanguageCode(String languageCode);
}
