package pga.magiccollectionspring.card.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pga.magiccollectionspring.card.domain.CardLocalization;
import pga.magiccollectionspring.card.domain.CardLocalizationId;
import pga.magiccollectionspring.card.domain.ICardLocalizationRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
public interface CardLocalizationRepository extends JpaRepository<CardLocalization, CardLocalizationId>, ICardLocalizationRepository {

    @Override
    @Query("""
            SELECT cl FROM CardLocalization cl
            WHERE cl.id.languageCode = :languageCode
            AND cl.id.oracleId IN :oracleIds
            """)
    List<CardLocalization> findByLanguageCodeAndOracleIds(
            @Param("languageCode") String languageCode,
            @Param("oracleIds") Set<String> oracleIds
    );

    @Override
    @Query("SELECT MAX(cl.lastUpdated) FROM CardLocalization cl")
    LocalDateTime findMaxLastUpdated();

    @Override
    @Query("""
            SELECT MAX(cl.lastUpdated) FROM CardLocalization cl
            WHERE cl.id.languageCode = :languageCode
            """)
    LocalDateTime findMaxLastUpdatedByLanguage(@Param("languageCode") String languageCode);

    @Override
    @Query("""
            SELECT cl.id.oracleId FROM CardLocalization cl
            WHERE cl.id.languageCode = :languageCode
            AND cl.lastUpdated > :since
            """)
    Set<String> findOracleIdsUpdatedSince(
            @Param("languageCode") String languageCode,
            @Param("since") LocalDateTime since
    );

    @Override
    default List<CardLocalization> findAllByIds(Iterable<CardLocalizationId> ids) {
        return findAllById(ids);
    }
}
