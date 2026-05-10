package pga.magiccollectionspring.card.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * DTO representing detailed information of a card.
 */
@Schema(description = "Detailed information of a Magic card")
public class CardDTO {
    @Schema(description = "Unique internal ID (UUID)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;
    
    @Schema(description = "Card name in English", example = "Black Lotus")
    private String name;
    
    @Schema(description = "Printed name (if translation exists)", example = "Loto Negro")
    private String printedName;
    
    @Schema(description = "Expansion/Set code", example = "LEA")
    private String setCode;
    
    @Schema(description = "Unique Scryfall ID", example = "bd8fa327-dd41-4737-8f19-2cf5eb1f7cdd")
    private String scryfallId;
    
    @Schema(description = "Card text (abilities)", example = "Add {B}{B}{B} to your mana pool.")
    private String oracleText;
    
    @Schema(description = "Type line", example = "Artifact")
    private String typeLine;
    
    @Schema(description = "Mana cost", example = "{0}")
    private String manaCost;
    
    @Schema(description = "Converted mana cost (legacy)")
    private Integer convertedManaCost;
    
    @Schema(description = "Mana Value (CMC)", example = "0.0")
    private Float cmc;
    
    @Schema(description = "Rarity", example = "rare")
    private String rarity;
    
    @Schema(description = "Numeric rarity rank")
    private Integer rarityRank;
    
    @Schema(description = "Binary color mask")
    private Integer colorMask;
    
    @Schema(description = "Binary color identity mask")
    private Integer identityMask;
    
    @Schema(description = "List of colors")
    private List<String> colors;
    
    @Schema(description = "List of color identity")
    private List<String> colorIdentity;
    
    @Schema(description = "Power", example = "0")
    private String power;
    
    @Schema(description = "Toughness", example = "0")
    private String toughness;
    
    @Schema(description = "URLs of the card images")
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

    @Schema(description = "Links to card images in different sizes")
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
