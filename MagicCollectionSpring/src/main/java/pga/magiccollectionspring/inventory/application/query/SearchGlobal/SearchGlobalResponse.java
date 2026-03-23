package pga.magiccollectionspring.inventory.application.query.SearchGlobal;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import java.util.List;

public record SearchGlobalResponse(List<CardYouOwnDTO> cards) {}