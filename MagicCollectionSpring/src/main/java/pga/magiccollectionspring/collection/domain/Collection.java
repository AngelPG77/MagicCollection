package pga.magiccollectionspring.collection.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pga.magiccollectionspring.user.domain.User;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "collections", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class Collection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    @OneToMany(mappedBy = "collection", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollectionCard> cards = new ArrayList<>();

    public Collection() {}

    public Collection(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public List<CollectionCard> getCards() { return cards; }
    public void setCards(List<CollectionCard> cards) { this.cards = cards; }

    public void addCard(CollectionCard card) {
        cards.add(card);
        card.setCollection(this);
    }

    public void removeCard(CollectionCard card) {
        cards.remove(card);
        card.setCollection(null);
    }
}