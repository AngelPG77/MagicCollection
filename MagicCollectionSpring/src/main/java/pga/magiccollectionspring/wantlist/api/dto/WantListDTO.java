package pga.magiccollectionspring.wantlist.api.dto;

import java.util.List;

public class WantListDTO {
    private Long id;
    private String name;
    private Long ownerId;
    private List<WantListCardDTO> cards;

    public WantListDTO() {}

    public WantListDTO(Long id, String name, Long ownerId, List<WantListCardDTO> cards) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.cards = cards;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public List<WantListCardDTO> getCards() { return cards; }
    public void setCards(List<WantListCardDTO> cards) { this.cards = cards; }
}
