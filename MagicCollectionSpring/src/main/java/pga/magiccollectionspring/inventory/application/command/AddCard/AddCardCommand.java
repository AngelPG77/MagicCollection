package pga.magiccollectionspring.inventory.application.command.AddCard;

public record AddCardCommand(Long collectionId, String cardName, Integer quantity, String condition, Boolean isFoil, String language) {}