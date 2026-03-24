package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;
import java.util.Optional;

/**
 * Domain repository interface for Card (master card catalog).
 * Handles all card catalog queries, separate from CardYouOwn (owned instances).
 */
public interface ICardRepository extends IRepository<Card, Long> {
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
    List<Card> findAllOrderByName();

    boolean existsByScryfallId(String scryfallId);
}