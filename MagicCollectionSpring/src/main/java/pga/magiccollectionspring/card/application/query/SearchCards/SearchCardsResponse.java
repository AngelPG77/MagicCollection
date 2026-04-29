package pga.magiccollectionspring.card.application.query.SearchCards;

import pga.magiccollectionspring.card.api.dto.CardDTO;
import java.util.List;

public record SearchCardsResponse(List<CardDTO> cards, int totalCards) {
}