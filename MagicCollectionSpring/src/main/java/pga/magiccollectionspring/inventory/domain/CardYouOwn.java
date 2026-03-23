package pga.magiccollectionspring.inventory.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.collection.domain.Collection;
import pga.magiccollectionspring.inventory.domain.enums.CardCondition;
import pga.magiccollectionspring.inventory.domain.enums.Language;
import jakarta.persistence.*;

@Entity
@Table(name = "cards_owned")
public class CardYouOwn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "is_foil", nullable = false)
    private boolean isFoil = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_condition", length = 20, nullable = false)
    private CardCondition cardCondition;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", nullable = false)
    @JsonIgnore
    private Collection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_master_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Card cardMasterData;

    public CardYouOwn() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @JsonProperty("isFoil")
    public boolean isFoil() { return isFoil; }
    public void setFoil(boolean foil) { isFoil = foil; }

    public CardCondition getCardCondition() { return cardCondition; }
    public void setCardCondition(CardCondition cardCondition) { this.cardCondition = cardCondition; }
    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }
    public Collection getCollection() { return collection; }
    public void setCollection(Collection collection) { this.collection = collection; }
    public Card getCardMasterData() { return cardMasterData; }
    public void setCardMasterData(Card cardMasterData) { this.cardMasterData = cardMasterData; }
}