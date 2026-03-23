package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ScryfallSearchResponse {

    @JsonProperty("total_cards")
    private int totalCards;

    @JsonProperty("data")
    private List<CardScryfallDTO> data;

    public ScryfallSearchResponse() {}

    public int getTotalCards() { return totalCards; }
    public void setTotalCards(int totalCards) { this.totalCards = totalCards; }
    public List<CardScryfallDTO> getData() { return data; }
    public void setData(List<CardScryfallDTO> data) { this.data = data; }
}