package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardScryfallDTO {

    @JsonProperty("id")
    private String scryfallId;

    @JsonProperty("name")
    private String name;

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

    public CardScryfallDTO() {}

    public CardScryfallDTO(String scryfallId, String name, String setCode, String oracleText, String typeLine, String manaCost, Double cmc) {
        this.scryfallId = scryfallId;
        this.name = name;
        this.setCode = setCode;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.cmc = cmc;
    }

    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
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
}
