package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IndexLanguageRowStateId implements Serializable {
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "scryfall_id", nullable = false, length = 64)
    private String scryfallId;

    public IndexLanguageRowStateId() {}

    public IndexLanguageRowStateId(String languageCode, String scryfallId) {
        this.languageCode = languageCode;
        this.scryfallId = scryfallId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getScryfallId() {
        return scryfallId;
    }

    public void setScryfallId(String scryfallId) {
        this.scryfallId = scryfallId;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IndexLanguageRowStateId that)) {
            return false;
        }
        return Objects.equals(languageCode, that.languageCode) && Objects.equals(scryfallId, that.scryfallId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(languageCode, scryfallId);
    }
}
