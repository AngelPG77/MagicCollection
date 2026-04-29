package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardScryfallDTO {

    @JsonProperty("id")
    private String scryfallId;

    @JsonProperty("oracle_id")
    private String oracleId;

    @JsonProperty("lang")
    private String lang;

    @JsonProperty("name")
    private String name;

    @JsonProperty("printed_name")
    private String printedName;

    @JsonProperty("set")
    private String setCode;

    @JsonProperty("oracle_text")
    private String oracleText;

    @JsonProperty("type_line")
    private String typeLine;

    @JsonProperty("mana_cost")
    private String manaCost;

    @JsonProperty("cmc")
    private Double cmc;

    @JsonProperty("rarity")
    private String rarity;

    @JsonProperty("colors")
    private List<String> colors;

    @JsonProperty("color_identity")
    private List<String> colorIdentity;

    @JsonProperty("power")
    private String power;

    @JsonProperty("toughness")
    private String toughness;

    @JsonProperty("image_uris")
    private ImageUrisDTO imageUris;

    @JsonProperty("card_faces")
    private List<CardFaceDTO> cardFaces;

    public CardScryfallDTO() {}

    public CardScryfallDTO(String scryfallId, String name, String setCode, String oracleText, String typeLine, String manaCost, Double cmc, String power, String toughness, ImageUrisDTO imageUris) {
        this.scryfallId = scryfallId;
        this.name = name;
        this.setCode = setCode;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.cmc = cmc;
        this.power = power;
        this.toughness = toughness;
        this.imageUris = imageUris;
    }

    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
    public String getOracleId() { return oracleId; }
    public void setOracleId(String oracleId) { this.oracleId = oracleId; }
    public String getLang() { return lang; }
    public void setLang(String lang) { this.lang = lang; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrintedName() { return printedName; }
    public void setPrintedName(String printedName) { this.printedName = printedName; }
    public String getSetCode() { return setCode; }
    public void setSetCode(String setCode) { this.setCode = setCode; }
    public String getOracleText() { return oracleText; }
    public void setOracleText(String oracleText) { this.oracleText = oracleText; }
    public String getTypeLine() { return typeLine; }
    public void setTypeLine(String typeLine) { this.typeLine = typeLine; }
    public String getManaCost() { return manaCost; }
    public void setManaCost(String manaCost) { this.manaCost = manaCost; }
    public Double getCmc() { return cmc; }
    public void setCmc(Double cmc) { this.cmc = cmc; }
    public String getPower() { return power; }
    public void setPower(String power) { this.power = power; }
    public String getToughness() { return toughness; }
    public void setToughness(String toughness) { this.toughness = toughness; }
    public String getRarity() { return rarity; }
    public void setRarity(String rarity) { this.rarity = rarity; }
    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }
    public List<String> getColorIdentity() { return colorIdentity; }
    public void setColorIdentity(List<String> colorIdentity) { this.colorIdentity = colorIdentity; }
    public ImageUrisDTO getImageUris() { return imageUris; }
    public void setImageUris(ImageUrisDTO imageUris) { this.imageUris = imageUris; }
    public List<CardFaceDTO> getCardFaces() { return cardFaces; }
    public void setCardFaces(List<CardFaceDTO> cardFaces) { this.cardFaces = cardFaces; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardFaceDTO {
        @JsonProperty("image_uris")
        private ImageUrisDTO imageUris;

        public CardFaceDTO() {}

        public ImageUrisDTO getImageUris() { return imageUris; }
        public void setImageUris(ImageUrisDTO imageUris) { this.imageUris = imageUris; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageUrisDTO {
        @JsonProperty("small")
        private String small;
        @JsonProperty("normal")
        private String normal;
        @JsonProperty("large")
        private String large;
        @JsonProperty("png")
        private String png;

        public ImageUrisDTO() {}

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
