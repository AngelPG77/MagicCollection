package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;

public interface IIndexLanguageRowStateRepository extends IRepository<IndexLanguageRowState, IndexLanguageRowStateId> {
    List<IndexLanguageRowState> findByIdLanguageCode(String languageCode);
}
