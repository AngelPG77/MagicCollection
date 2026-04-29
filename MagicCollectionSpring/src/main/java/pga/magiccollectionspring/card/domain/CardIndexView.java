package pga.magiccollectionspring.card.domain;

public interface CardIndexView {
    String getOracleId();

    String getScryfallId();

    String getName();

    Integer getColorMask();

    Integer getIdentityMask();

    String getManaCost();

    Float getCmc();

    Integer getRarityRank();

    String getTypeLine();

    String getSetCode();

    String getImageSmallUrl();
}
