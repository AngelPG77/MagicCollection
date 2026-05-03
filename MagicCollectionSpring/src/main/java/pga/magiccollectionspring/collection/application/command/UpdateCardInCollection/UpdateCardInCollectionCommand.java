package pga.magiccollectionspring.collection.application.command.UpdateCardInCollection;

public record UpdateCardInCollectionCommand(
    Long collectionId,
    Long cardId,
    Integer quantity,
    Boolean foil,
    String language,
    String condition
) {}
