package pga.magiccollectionspring.collection.application.command.AddCardToCollection;

public record AddCardToCollectionCommand(
    Long collectionId,
    String scryfallId,
    String name,
    String typeLine,
    String manaCost,
    String imageUrl,
    Integer quantity,
    Boolean foil,
    String language,
    String condition
) {}
