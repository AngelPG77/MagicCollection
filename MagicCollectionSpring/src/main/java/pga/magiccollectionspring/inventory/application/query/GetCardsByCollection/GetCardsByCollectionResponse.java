package pga.magiccollectionspring.inventory.application.query.GetCardsByCollection;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import java.util.List;

public record GetCardsByCollectionResponse(List<CardYouOwnDTO> cards) {}