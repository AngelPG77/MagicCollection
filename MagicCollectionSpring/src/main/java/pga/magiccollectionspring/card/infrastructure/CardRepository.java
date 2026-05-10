package pga.magiccollectionspring.card.infrastructure;

import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.CardIndexView;
import pga.magiccollectionspring.card.domain.ICardRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Spring Data JPA Repository for Card (MasterCard) entity.
 * Handles persistence operations for the card library/catalog.
 */
@Repository
public interface CardRepository extends JpaRepository<Card, String>, ICardRepository {

    // ========== BASIC LOOKUPS ==========
    Optional<Card> findByScryfallId(String scryfallId);

    Optional<Card> findByNameIgnoreCase(String name);

    // ========== SEARCH BY CARD PROPERTIES ==========
    /**
     * Search cards by exact type line match
     * (e.g., "Creature", "Instant", "Enchantment — Aura")
     */
    @Query("SELECT c FROM Card c WHERE LOWER(c.typeLine) LIKE LOWER(CONCAT('%', :type, '%'))")
    List<Card> findByTypeLineContainingIgnoreCase(@Param("type") String type);

    /**
     * Search cards by partial name match
     */
    @Query("SELECT c FROM Card c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Card> searchByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Global search across name and oracle text
     * Used for discovering cards by keyword/ability
     */
    @Query("SELECT c FROM Card c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
            "LOWER(c.oracleText) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "ORDER BY c.name ASC")
    List<Card> globalSearch(@Param("term") String term);

    // ========== SEARCH BY COLOR ==========
    /**
     * Search cards by mana color in cost
     * (e.g., 'W' for white, 'U' for blue, 'B' for black, 'R' for red, 'G' for green)
     */
    @Query("SELECT c FROM Card c WHERE c.manaCost LIKE CONCAT('%', :color, '%')")
    List<Card> findByColor(@Param("color") String color);

    // ========== SEARCH BY MANA COST ==========
    /**
     * Search cards by converted mana cost
     */
    @Query("SELECT c FROM Card c WHERE c.convertedManaCost = :cmc")
    List<Card> findByConvertedManaCost(@Param("cmc") Integer cmc);

    /**
     * Search cards by mana cost range
     */
    @Query("SELECT c FROM Card c WHERE c.convertedManaCost BETWEEN :minCmc AND :maxCmc")
    List<Card> findByConvertedManaCostBetween(
            @Param("minCmc") Integer minCmc,
            @Param("maxCmc") Integer maxCmc
    );

    // ========== SEARCH BY SET ==========
    /**
     * Get all cards from a specific set
     */
    @Query("SELECT c FROM Card c WHERE c.setCode = :setCode ORDER BY c.name ASC")
    List<Card> findBySetCode(@Param("setCode") String setCode);

    // ========== ADVANCED FILTERS ==========
    /**
     * Find cards by color AND type (e.g., blue creatures)
     */
    @Query("SELECT c FROM Card c WHERE " +
            "c.manaCost LIKE CONCAT('%', :color, '%') AND " +
            "LOWER(c.typeLine) LIKE LOWER(CONCAT('%', :type, '%'))")
    List<Card> findByColorAndType(@Param("color") String color, @Param("type") String type);

    /**
     * Optimization: Get all existing Scryfall IDs in one go
     */
    @Query("SELECT c.scryfallId FROM Card c")
    Set<String> findAllScryfallIds();

    // ========== EXISTENCE CHECKS ==========
    boolean existsByScryfallId(String scryfallId);

    @Query("SELECT MAX(c.lastUpdated) FROM Card c")
    LocalDateTime findMaxLastUpdated();

    @Override
    @Query(
            value = """
                    SELECT
                        c.oracleId AS oracleId,
                        c.scryfallId AS scryfallId,
                        c.name AS name,
                        c.colorMask AS colorMask,
                        c.identityMask AS identityMask,
                        c.manaCost AS manaCost,
                        c.cmc AS cmc,
                        c.rarityRank AS rarityRank,
                        c.typeLine AS typeLine,
                        c.setCode AS setCode,
                        c.imageSmallUrl AS imageSmallUrl
                    FROM Card c
                    """,
            countQuery = "SELECT COUNT(c) FROM Card c"
    )
    Page<CardIndexView> findIndexPage(Pageable pageable);

    @Override
    @Query("""
            SELECT
                c.oracleId AS oracleId,
                c.scryfallId AS scryfallId,
                c.name AS name,
                c.colorMask AS colorMask,
                c.identityMask AS identityMask,
                c.manaCost AS manaCost,
                c.cmc AS cmc,
                c.rarityRank AS rarityRank,
                c.typeLine AS typeLine,
                c.setCode AS setCode,
                c.imageSmallUrl AS imageSmallUrl
            FROM Card c
            """)
    Slice<CardIndexView> findIndexSlice(Pageable pageable);

    @Override
    @Query("""
            SELECT
                c.oracleId AS oracleId,
                c.scryfallId AS scryfallId,
                c.name AS name,
                c.colorMask AS colorMask,
                c.identityMask AS identityMask,
                c.manaCost AS manaCost,
                c.cmc AS cmc,
                c.rarityRank AS rarityRank,
                c.typeLine AS typeLine,
                c.setCode AS setCode,
                c.imageSmallUrl AS imageSmallUrl
            FROM Card c
            WHERE c.scryfallId IN :scryfallIds
            ORDER BY c.scryfallId ASC
            """)
    List<CardIndexView> findIndexByScryfallIds(@Param("scryfallIds") Set<String> scryfallIds);

    @Override
    @Query("SELECT c.scryfallId FROM Card c WHERE c.lastUpdated > :since")
    Set<String> findScryfallIdsUpdatedSince(@Param("since") LocalDateTime since);

    @Override
    @Query("SELECT c.scryfallId FROM Card c WHERE c.oracleId IN :oracleIds")
    Set<String> findScryfallIdsByOracleIds(@Param("oracleIds") Set<String> oracleIds);
}
