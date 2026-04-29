package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CardLocalizationId implements Serializable {

    @Column(name = "oracle_id", nullable = false, length = 64)
    private String oracleId;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    public CardLocalizationId() {}

    public CardLocalizationId(String oracleId, String languageCode) {
        this.oracleId = oracleId;
        this.languageCode = languageCode;
    }

    public String getOracleId() {
        return oracleId;
    }

    public void setOracleId(String oracleId) {
        this.oracleId = oracleId;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CardLocalizationId that)) {
            return false;
        }
        return Objects.equals(oracleId, that.oracleId) && Objects.equals(languageCode, that.languageCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oracleId, languageCode);
    }
}
