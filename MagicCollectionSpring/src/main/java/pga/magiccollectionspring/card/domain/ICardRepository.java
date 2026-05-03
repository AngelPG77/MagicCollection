package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Domain repository interface for Card (master card catalog).
 * Handles all card catalog queries, separate from CardYouOwn (owned instances).
 */
public interface ICardRepository extends IRepository<Card, String> {
    // Basic lookups
    Optional<Card> findByScryfallId(String scryfallId);

    Optional<Card> findByNameIgnoreCase(String name);

    // Search by card properties
    List<Card> findByTypeLineContainingIgnoreCase(String type);

    List<Card> searchByNameContainingIgnoreCase(String name);

    List<Card> globalSearch(String term);

    // Search by color
    List<Card> findByColor(String color);

    // Search by mana cost
    List<Card> findByConvertedManaCost(Integer cmc);

    List<Card> findByConvertedManaCostBetween(Integer minCmc, Integer maxCmc);

    // Search by set
    List<Card> findBySetCode(String setCode);

    // Advanced filters
    List<Card> findByColorAndType(String color, String type);

    // Utility
    Set<String> findAllScryfallIds();

    boolean existsByScryfallId(String scryfallId);

    long count();

    LocalDateTime findMaxLastUpdated();

    Page<CardIndexView> findIndexPage(Pageable pageable);

    Slice<CardIndexView> findIndexSlice(Pageable pageable);

    List<CardIndexView> findIndexByScryfallIds(Set<String> scryfallIds);

    Set<String> findScryfallIdsUpdatedSince(LocalDateTime since);

    Set<String> findScryfallIdsByOracleIds(Set<String> oracleIds);

}
