package pga.magiccollectionspring.card.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "master_cards", indexes = {
        @Index(name = "idx_card_name", columnList = "name"),
        @Index(name = "idx_card_search_filters", columnList = "color_mask, cmc, rarity_rank"),
        @Index(name = "idx_card_set", columnList = "set_code")
})
public class Card {

    @Id
    @Column(name = "scryfall_id", length = 64)
    private String scryfallId;

    @Column(nullable = false)
    private String name;

    @Column(name = "printed_name")
    private String printedName;

    @Column(name = "set_code", nullable = false, length = 10)
    private String setCode;

    @Column(name = "oracle_id", length = 64)
    private String oracleId;

    @Column(name = "oracle_text", columnDefinition = "TEXT")
    private String oracleText;

    @Column(name = "type_line")
    private String typeLine;

    @Column(name = "mana_cost")
    private String manaCost;

    @Column(name = "converted_mana_cost")
    private Integer convertedManaCost;

    @Column(name = "cmc")
    private Float cmc;

    @Column(name = "rarity", length = 32)
    private String rarity;

    @Column(name = "rarity_rank", nullable = false)
    private Integer rarityRank = 0;

    @Column(name = "color_mask", nullable = false)
    private Integer colorMask = 0;

    @Column(name = "identity_mask", nullable = false)
    private Integer identityMask = 0;

    @Column(name = "image_small_url", length = 1024)
    private String imageSmallUrl;

    @Column(name = "last_updated")
    private java.time.LocalDateTime lastUpdated;

    public Card() {}

    public Card(String scryfallId,
                String name,
                String printedName,
                String setCode,
                String oracleId,
                String oracleText,
                String typeLine,
                String manaCost,
                Integer convertedManaCost,
                Float cmc,
                String rarity,
                Integer rarityRank,
                Integer colorMask,
                Integer identityMask,
                String imageSmallUrl) {
        this.scryfallId = scryfallId;
        this.name = name;
        this.printedName = printedName;
        this.setCode = setCode;
        this.oracleId = oracleId;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.convertedManaCost = convertedManaCost;
        this.cmc = cmc;
        this.rarity = rarity;
        this.rarityRank = rarityRank;
        this.colorMask = colorMask;
        this.identityMask = identityMask;
        this.imageSmallUrl = imageSmallUrl;
    }

    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrintedName() { return printedName; }
    public void setPrintedName(String printedName) { this.printedName = printedName; }
    public String getSetCode() { return setCode; }
    public void setSetCode(String setCode) { this.setCode = setCode; }
    public String getOracleId() { return oracleId; }
    public void setOracleId(String oracleId) { this.oracleId = oracleId; }
    public String getOracleText() { return oracleText; }
    public void setOracleText(String oracleText) { this.oracleText = oracleText; }
    public String getTypeLine() { return typeLine; }
    public void setTypeLine(String typeLine) { this.typeLine = typeLine; }
    public String getManaCost() { return manaCost; }
    public void setManaCost(String manaCost) { this.manaCost = manaCost; }
    public Integer getConvertedManaCost() { return convertedManaCost; }
    public void setConvertedManaCost(Integer convertedManaCost) { this.convertedManaCost = convertedManaCost; }
    public Float getCmc() { return cmc; }
    public void setCmc(Float cmc) { this.cmc = cmc; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public Integer getRarityRank() { return rarityRank; }
    public void setRarityRank(Integer rarityRank) { this.rarityRank = rarityRank; }
    public Integer getColorMask() { return colorMask; }
    public void setColorMask(Integer colorMask) { this.colorMask = colorMask; }
    public Integer getIdentityMask() { return identityMask; }
    public void setIdentityMask(Integer identityMask) { this.identityMask = identityMask; }
    public String getImageSmallUrl() { return imageSmallUrl; }
    public void setImageSmallUrl(String imageSmallUrl) { this.imageSmallUrl = imageSmallUrl; }
    public java.time.LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(java.time.LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
}
