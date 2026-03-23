package pga.magiccollectionspring.card.application.query.GetAllCards;

import pga.magiccollectionspring.card.api.dto.CardDTO;
import java.util.List;

public record GetAllCardsResponse(List<CardDTO> cards) {}