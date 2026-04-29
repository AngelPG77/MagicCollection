package pga.magiccollectionspring.wantlist.application.command.UpdateCardInWantList;

public record UpdateCardInWantListCommand(
    Long wantListId,
    Long cardId,
    Integer quantity,
    Boolean foil,
    String language,
    String condition
) {}
