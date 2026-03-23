package pga.magiccollectionspring.inventory.application.command.UpdateCard;

public record UpdateCardCommand(Long id, Integer quantity, String condition, Boolean isFoil, String language) {}