package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "index_language_delta_entry", indexes = {
        @Index(name = "idx_index_lang_delta_lang_version", columnList = "language_code,target_version_long"),
        @Index(name = "idx_index_lang_delta_lang_card", columnList = "language_code,scryfall_id")
})
public class IndexLanguageDeltaEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "target_version", nullable = false, length = 64)
    private String targetVersion;

    @Column(name = "target_version_long", nullable = false)
    private long targetVersionLong;

    @Column(name = "scryfall_id", nullable = false, length = 64)
    private String scryfallId;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 16)
    private LanguageIndexDeltaChangeType changeType;

    @Column(name = "localized_name")
    private String localizedName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public IndexLanguageDeltaEntry() {}

    public Long getId() {
        return id;
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getTargetVersion() {
        return targetVersion;
    }

    public void setTargetVersion(String targetVersion) {
        this.targetVersion = targetVersion;
    }

    public long getTargetVersionLong() {
        return targetVersionLong;
    }

    public void setTargetVersionLong(long targetVersionLong) {
        this.targetVersionLong = targetVersionLong;
    }

    public String getScryfallId() {
        return scryfallId;
    }

    public void setScryfallId(String scryfallId) {
        this.scryfallId = scryfallId;
    }

    public LanguageIndexDeltaChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(LanguageIndexDeltaChangeType changeType) {
        this.changeType = changeType;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
