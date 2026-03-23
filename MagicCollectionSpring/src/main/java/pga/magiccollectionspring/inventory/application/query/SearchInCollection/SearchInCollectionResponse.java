package pga.magiccollectionspring.inventory.application.query.SearchInCollection;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import java.util.List;

public record SearchInCollectionResponse(List<CardYouOwnDTO> cards) {}