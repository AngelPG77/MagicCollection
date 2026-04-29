package pga.magiccollectionspring.card.api.dto;

import java.util.List;

public class CardDTO {
    private String id;
    private String name;
    private String printedName;
    private String setCode;
    private String scryfallId;
    private String oracleText;
    private String typeLine;
    private String manaCost;
    private Integer convertedManaCost;
    private Float cmc;
    private String rarity;
    private Integer rarityRank;
    private Integer colorMask;
    private Integer identityMask;
    private List<String> colors;
    private List<String> colorIdentity;
    private String power;
    private String toughness;
    private ImageUrisDTO imageUris;

    public CardDTO() {}

    public CardDTO(String id,
                   String name,
                   String printedName,
                   String setCode,
                   String scryfallId,
                   String oracleText,
                   String typeLine,
                   String manaCost,
                   Integer convertedManaCost,
                   Float cmc,
                   String rarity,
                   Integer rarityRank,
                   Integer colorMask,
                   Integer identityMask,
                   List<String> colors,
                   List<String> colorIdentity,
                   String power,
                   String toughness,
                   ImageUrisDTO imageUris) {
        this.id = id;
        this.name = name;
        this.printedName = printedName;
        this.setCode = setCode;
        this.scryfallId = scryfallId;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.convertedManaCost = convertedManaCost;
        this.cmc = cmc;
        this.rarity = rarity;
        this.rarityRank = rarityRank;
        this.colorMask = colorMask;
        this.identityMask = identityMask;
        this.colors = colors;
        this.colorIdentity = colorIdentity;
        this.power = power;
        this.toughness = toughness;
        this.imageUris = imageUris;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrintedName() { return printedName; }
    public void setPrintedName(String printedName) { this.printedName = printedName; }
    public String getSetCode() { return setCode; }
    public void setSetCode(String setCode) { this.setCode = setCode; }
    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
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
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public List<String> getColorIdentity() { return colorIdentity; }
    public void setColorIdentity(List<String> colorIdentity) { this.colorIdentity = colorIdentity; }
    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }
    public String getToughness() { return toughness; }
    public void setToughness(String toughness) { this.toughness = toughness; }
    public ImageUrisDTO getImageUris() { return imageUris; }
    public void setImageUris(ImageUrisDTO imageUris) { this.imageUris = imageUris; }

    public static class ImageUrisDTO {
        private String small;
        private String normal;
        private String large;
        private String png;

        public ImageUrisDTO() {}

        public ImageUrisDTO(String small, String normal, String large, String png) {
            this.small = small;
            this.normal = normal;
            this.large = large;
            this.png = png;
        }

        public String getSmall() { return small; }
        public void setSmall(String small) { this.small = small; }
        public String getNormal() { return normal; }
        public void setNormal(String normal) { this.normal = normal; }
        public String getLarge() { return large; }
        public void setLarge(String large) { this.large = large; }
        public String getPng() { return png; }
        public void setPng(String png) { this.png = png; }
    }
}
