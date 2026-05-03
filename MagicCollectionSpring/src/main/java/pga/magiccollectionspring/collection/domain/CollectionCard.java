package pga.magiccollectionspring.collection.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "collection_cards")
public class CollectionCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonIgnore
    private Collection collection;

    @Column(nullable = false)
    private String scryfallId;

    @Column(nullable = false)
    private String name;

    private String typeLine;

    private String manaCost;

    private String imageUrl;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(nullable = false)
    private Boolean foil = false;

    @Column(name = "`condition`", length = 20)
    private String condition = "NEAR_MINT";

    @Column(length = 10)
    private String language = "en";

    public CollectionCard() {}

    public CollectionCard(String scryfallId, String name, String typeLine, String manaCost, 
                        String imageUrl, Integer quantity, Boolean foil, String language, String condition) {
        this.scryfallId = scryfallId;
        this.name = name;
        this.typeLine = typeLine;
        this.manaCost = manaCost;
        this.imageUrl = imageUrl;
        this.quantity = quantity != null ? quantity : 1;
        this.foil = foil != null ? foil : false;
        this.language = language != null ? language : "en";
        this.condition = condition != null ? condition : "NEAR_MINT";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Collection getCollection() { return collection; }
    public void setCollection(Collection collection) { this.collection = collection; }
    public String getScryfallId() { return scryfallId; }
    public void setScryfallId(String scryfallId) { this.scryfallId = scryfallId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTypeLine() { return typeLine; }
    public void setTypeLine(String typeLine) { this.typeLine = typeLine; }
    public String getManaCost() { return manaCost; }
    public void setManaCost(String manaCost) { this.manaCost = manaCost; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Boolean getFoil() { return foil; }
    public void setFoil(Boolean foil) { this.foil = foil; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
