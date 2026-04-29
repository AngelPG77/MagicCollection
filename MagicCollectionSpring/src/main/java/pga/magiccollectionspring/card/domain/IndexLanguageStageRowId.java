package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class IndexLanguageStageRowId implements Serializable {
    @Column(name = "build_token", nullable = false, length = 64)
    private String buildToken;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "scryfall_id", nullable = false, length = 64)
    private String scryfallId;

    public IndexLanguageStageRowId() {}

    public IndexLanguageStageRowId(String buildToken, String languageCode, String scryfallId) {
        this.buildToken = buildToken;
        this.languageCode = languageCode;
        this.scryfallId = scryfallId;
    }

    public String getBuildToken() {
        return buildToken;
    }

    public void setBuildToken(String buildToken) {
        this.buildToken = buildToken;
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
        if (!(o instanceof IndexLanguageStageRowId that)) {
            return false;
        }
        return Objects.equals(buildToken, that.buildToken) &&
                Objects.equals(languageCode, that.languageCode) &&
                Objects.equals(scryfallId, that.scryfallId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(buildToken, languageCode, scryfallId);
    }
}
