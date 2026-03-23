package pga.magiccollectionspring.card.domain;

import pga.magiccollectionspring.shared.abstractions.IRepository;

import java.util.Optional;

public interface ICardRepository extends IRepository<Card, Long> {
    Optional<Card> findByScryfallId(String scryfallId);
    Optional<Card> findByNameIgnoreCase(String name);
}