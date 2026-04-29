package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pga.magiccollectionspring.card.domain.IIndexLanguageStageRowRepository;
import pga.magiccollectionspring.card.domain.IndexLanguageStageRow;
import pga.magiccollectionspring.card.domain.IndexLanguageStageRowId;

import java.util.List;

@Repository
public interface IndexLanguageStageRowRepository extends JpaRepository<IndexLanguageStageRow, IndexLanguageStageRowId>, IIndexLanguageStageRowRepository {
    List<IndexLanguageStageRow> findByIdBuildTokenAndIdLanguageCode(String buildToken, String languageCode);

    @Modifying
    @Transactional
    @Query("delete from IndexLanguageStageRow r where r.id.buildToken = :buildToken and r.id.languageCode = :languageCode")
    void deleteByIdBuildTokenAndIdLanguageCode(@Param("buildToken") String buildToken, @Param("languageCode") String languageCode);
}
