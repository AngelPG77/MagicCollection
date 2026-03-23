package pga.magiccollectionspring.inventory.application.query.SearchByType;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;
import java.util.List;

public record SearchByTypeResponse(List<CardYouOwnDTO> cards) {}