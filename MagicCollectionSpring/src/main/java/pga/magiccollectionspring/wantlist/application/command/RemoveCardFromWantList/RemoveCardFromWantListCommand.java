package pga.magiccollectionspring.wantlist.application.command.RemoveCardFromWantList;

public record RemoveCardFromWantListCommand(Long wantListId, Long cardId) {}
