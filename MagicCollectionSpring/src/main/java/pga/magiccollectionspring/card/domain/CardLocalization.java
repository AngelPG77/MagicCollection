package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "card_localizations", indexes = {
        @Index(name = "idx_card_loc_lang", columnList = "language_code"),
        @Index(name = "idx_card_loc_oracle", columnList = "oracle_id")
})
public class CardLocalization {

    @EmbeddedId
    private CardLocalizationId id;

    @Column(name = "localized_name", nullable = false)
    private String localizedName;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public CardLocalization() {}

    public CardLocalization(CardLocalizationId id, String localizedName, LocalDateTime lastUpdated) {
        this.id = id;
        this.localizedName = localizedName;
        this.lastUpdated = lastUpdated;
    }

    public CardLocalizationId getId() {
        return id;
    }

    public void setId(CardLocalizationId id) {
        this.id = id;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
