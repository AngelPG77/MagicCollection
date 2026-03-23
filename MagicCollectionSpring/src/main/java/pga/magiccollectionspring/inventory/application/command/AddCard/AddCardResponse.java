package pga.magiccollectionspring.inventory.application.command.AddCard;

import pga.magiccollectionspring.inventory.api.dto.CardYouOwnDTO;

public record AddCardResponse(CardYouOwnDTO card) {}