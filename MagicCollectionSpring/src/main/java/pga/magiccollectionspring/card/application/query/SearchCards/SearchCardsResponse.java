package pga.magiccollectionspring.card.application.query.SearchCards;

import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;

import java.util.List;

public record SearchCardsResponse(List<CardScryfallDTO> cards, int total) {}