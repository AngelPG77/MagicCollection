package pga.magiccollectionspring.inventory.application.command.UpdateCard;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;

public record UpdateCardResponse(CardYouOwnDTO card) {}