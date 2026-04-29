package pga.magiccollectionspring.wantlist.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import pga.magiccollectionspring.user.domain.User;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "want_lists", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class WantList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User owner;

    @OneToMany(mappedBy = "wantList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WantListCard> cards = new ArrayList<>();

    public WantList() {}

    public WantList(String name, User owner) {
        this.name = name;
        this.owner = owner;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public List<WantListCard> getCards() { return cards; }
    public void setCards(List<WantListCard> cards) { this.cards = cards; }

    public void addCard(WantListCard card) {
        cards.add(card);
        card.setWantList(this);
    }

    public void removeCard(WantListCard card) {
        cards.remove(card);
        card.setWantList(null);
    }
}
