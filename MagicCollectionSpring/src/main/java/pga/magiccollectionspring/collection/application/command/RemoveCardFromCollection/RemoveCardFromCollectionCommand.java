package pga.magiccollectionspring.collection.application.command.RemoveCardFromCollection;

public record RemoveCardFromCollectionCommand(
    Long collectionId,
    Long cardId
) {}
