package pga.magiccollectionspring.card.application;

import pga.magiccollectionspring.card.domain.Card;
import java.util.Optional;
import java.util.List;

public interface ICardInternalService {
    Card getOrFetchCard(String name, String lang);
    Optional<Card> findById(String scryfallId);
    List<String> findScryfallIdsByType(String type);
}
