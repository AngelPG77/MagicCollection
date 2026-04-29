package pga.magiccollectionspring.card.application.query.SearchCards;

public record SearchCardsQuery(
    String query,
    String colors,
    Boolean colorIdentity,
    String colorLogic,
    String type,
    String text,
    String manaCost,
    String set,
    String rarity,
    String artist,
    String lang
) {
    public SearchCardsQuery(String query, String lang) {
        this(query, null, false, null, null, null, null, null, null, null, lang);
    }
}
