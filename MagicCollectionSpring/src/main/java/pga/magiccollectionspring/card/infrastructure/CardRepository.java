package pga.magiccollectionspring.card.infrastructure;

import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.ICardRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, ICardRepository {
    Optional<Card> findByScryfallId(String scryfallId);
    Optional<Card> findByNameIgnoreCase(String name);
}