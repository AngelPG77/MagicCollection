package pga.magiccollectionspring.wantlist.application.command.AddCardToWantList;

public record AddCardToWantListCommand(
    Long wantListId,
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
