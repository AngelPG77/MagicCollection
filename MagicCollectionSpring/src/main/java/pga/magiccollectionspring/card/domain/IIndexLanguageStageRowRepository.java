package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;

public interface IIndexLanguageStageRowRepository extends IRepository<IndexLanguageStageRow, IndexLanguageStageRowId> {
    List<IndexLanguageStageRow> findByIdBuildTokenAndIdLanguageCode(String buildToken, String languageCode);

    void deleteByIdBuildTokenAndIdLanguageCode(String buildToken, String languageCode);
}
