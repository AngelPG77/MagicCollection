package pga.magiccollectionspring.inventory.domain;

import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CardYouOwn aggregate.
 * Handles queries about owned card instances, not the card catalog.
 */
public interface ICardYouOwnRepository extends IRepository<CardYouOwn, Long> {
    // Collection-based queries
    List<CardYouOwn> findByCollection_Id(Long collectionId);

    // Find specific card variant (same condition, foil, language)
    Optional<CardYouOwn> findExactCardInCollection(
            Long collectionId,
            Long cardId,
            CardCondition condition,
            boolean foil,
            Language language
    );

    // Filter by instance properties
    List<CardYouOwn> findByCollectionAndCondition(Long collectionId, CardCondition condition);

    List<CardYouOwn> findFoilCardsInCollection(Long collectionId);

    List<CardYouOwn> findByCollectionAndLanguage(Long collectionId, Language language);

    // Statistics
    Integer getTotalCardsQuantity(Long collectionId);

    Integer getUniqueCardsCount(Long collectionId);

    // Note: Searches by card type/text/name should use CardRepository instead
}