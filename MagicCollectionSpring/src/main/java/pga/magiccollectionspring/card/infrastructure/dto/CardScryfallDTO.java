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

    public CardScryfallDTO() {}

    public CardScryfallDTO(String scryfallId, String name, String setCode, String oracleText, String typeLine) {
        this.scryfallId = scryfallId;
        this.name = name;
        this.setCode = setCode;
        this.oracleText = oracleText;
        this.typeLine = typeLine;
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
}