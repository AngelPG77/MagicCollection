package pga.magiccollectionspring.inventory.infrastructure;

import pga.magiccollectionspring.inventory.domain.CardYouOwn;
import pga.magiccollectionspring.inventory.domain.ICardYouOwnRepository;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for CardYouOwn entity.
 * Handles persistence of user-owned card instances with their properties
 * (condition, foil status, language, quantity).
 * 
 * NOTE: Searches by card properties (type, text) are in CardRepository.
 * This repository focuses on "owned instance" queries, not card catalog queries.
 */
@Repository
public interface CardYouOwnRepository extends JpaRepository<CardYouOwn, Long>, ICardYouOwnRepository {

    // ========== COLLECTION-BASED QUERIES ==========
    /**
     * Get all card instances owned in a specific collection
     */
    List<CardYouOwn> findByCollection_Id(Long collectionId);

    // ========== OWNED-INSTANCE SPECIFIC SEARCHES ==========
    /**
     * Find a specific card variant (same card, condition, foil, language)
     * Used to avoid duplicating the same card with different conditions
     */
    @Query("SELECT c FROM CardYouOwn c WHERE " +
            "c.collection.id = :collectionId AND " +
            "c.cardMasterData.id = :cardId AND " +
            "c.cardCondition = :condition AND " +
            "c.isFoil = :foil AND " +
            "c.language = :language")
    Optional<CardYouOwn> findExactCardInCollection(
            @Param("collectionId") Long collectionId,
            @Param("cardId") Long cardId,
            @Param("condition") CardCondition condition,
            @Param("foil") boolean foil,
            @Param("language") Language language
    );

    // ========== FILTER BY INSTANCE PROPERTIES ==========
    /**
     * Get all cards with a specific condition in a collection
     */
    @Query("SELECT c FROM CardYouOwn c WHERE " +
            "c.collection.id = :collectionId AND " +
            "c.cardCondition = :condition")
    List<CardYouOwn> findByCollectionAndCondition(
            @Param("collectionId") Long collectionId,
            @Param("condition") CardCondition condition
    );

    /**
     * Get all foil copies in a collection
     */
    @Query("SELECT c FROM CardYouOwn c WHERE " +
            "c.collection.id = :collectionId AND " +
            "c.isFoil = true")
    List<CardYouOwn> findFoilCardsInCollection(@Param("collectionId") Long collectionId);

    /**
     * Get all cards in a specific language in a collection
     */
    @Query("SELECT c FROM CardYouOwn c WHERE " +
            "c.collection.id = :collectionId AND " +
            "c.language = :language")
    List<CardYouOwn> findByCollectionAndLanguage(
            @Param("collectionId") Long collectionId,
            @Param("language") Language language
    );

    // ========== STATISTICS ==========
    /**
     * Count total cards (by quantity) in a collection
     */
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CardYouOwn c WHERE c.collection.id = :collectionId")
    Integer getTotalCardsQuantity(@Param("collectionId") Long collectionId);

    /**
     * Count unique card definitions in a collection
     */
    @Query("SELECT COUNT(DISTINCT c.cardMasterData.id) FROM CardYouOwn c WHERE c.collection.id = :collectionId")
    Integer getUniqueCardsCount(@Param("collectionId") Long collectionId);
}