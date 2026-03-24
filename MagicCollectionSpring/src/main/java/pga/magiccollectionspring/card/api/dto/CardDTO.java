package pga.magiccollectionspring.card.api.dto;

public class CardDTO {
    private Long id;
    private String name;
    private String setCode;
    private String scryfallId;
    private String oracleText;
    private String typeLine;
    private String manaCost;
    private Integer convertedManaCost;

    public CardDTO() {}

    public CardDTO(Long id, String name, String setCode, String scryfallId, String oracleText, String typeLine, String manaCost, Integer convertedManaCost) {
        this.id = id;
        this.name = name;
        this.setCode = setCode;
        this.scryfallId = scryfallId;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.convertedManaCost = convertedManaCost;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
}
