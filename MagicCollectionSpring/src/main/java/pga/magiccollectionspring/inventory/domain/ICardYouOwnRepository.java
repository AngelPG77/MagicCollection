package pga.magiccollectionspring.inventory.domain;

import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.List;
import java.util.Optional;

public interface ICardYouOwnRepository extends IRepository<CardYouOwn, Long> {
    List<CardYouOwn> findByCollection_Id(Long collectionId);
    List<CardYouOwn> searchInMyGlobalInventory(Long userId, String term);
    List<CardYouOwn> searchInSpecificCollection(Long collId, String term);
    List<CardYouOwn> searchMyCardsByType(Long userId, String type);
    Optional<CardYouOwn> findExactCardInCollection(Long collectionId, Long cardId, CardCondition condition, boolean foil, Language language);
}