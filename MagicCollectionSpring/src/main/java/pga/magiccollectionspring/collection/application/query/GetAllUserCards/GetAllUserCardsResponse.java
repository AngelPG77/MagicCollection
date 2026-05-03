package pga.magiccollectionspring.collection.application.query.GetAllUserCards;

import pga.magiccollectionspring.collection.api.dto.CollectionCardDTO;

import java.util.List;

public record GetAllUserCardsResponse(List<CollectionCardDTO> cards) {}
