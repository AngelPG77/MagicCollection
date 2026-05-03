package pga.magiccollectionspring.collection.api.dto;

import java.util.List;

public class CollectionDTO {
    private Long id;
    private String name;
    private String ownerUsername;
    private Integer cardCount;
    private List<CollectionCardDTO> cards;

    public CollectionDTO() {}

    public CollectionDTO(Long id, String name, String ownerUsername, Integer cardCount, List<CollectionCardDTO> cards) {
        this.id = id;
        this.name = name;
        this.ownerUsername = ownerUsername;
        this.cardCount = cardCount;
        this.cards = cards;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getOwnerUsername() { return ownerUsername; }
    public void setOwnerUsername(String ownerUsername) { this.ownerUsername = ownerUsername; }
    public Integer getCardCount() { return cardCount; }
    public void setCardCount(Integer cardCount) { this.cardCount = cardCount; }
    public List<CollectionCardDTO> getCards() { return cards; }
    public void setCards(List<CollectionCardDTO> cards) { this.cards = cards; }
}