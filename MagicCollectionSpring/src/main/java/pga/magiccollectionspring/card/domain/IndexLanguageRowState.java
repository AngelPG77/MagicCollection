package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "index_language_row_state", indexes = {
        @Index(name = "idx_index_lang_row_lang_deleted", columnList = "language_code, deleted"),
        @Index(name = "idx_index_lang_row_last_version", columnList = "last_version")
})
public class IndexLanguageRowState {
    @EmbeddedId
    private IndexLanguageRowStateId id;

    @Column(name = "localized_name", nullable = false)
    private String localizedName;

    @Column(name = "row_hash", nullable = false, length = 64)
    private String rowHash;

    @Column(name = "last_version", nullable = false, length = 64)
    private String lastVersion;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public IndexLanguageRowState() {}

    public IndexLanguageRowStateId getId() {
        return id;
    }

    public void setId(IndexLanguageRowStateId id) {
        this.id = id;
    }

    public String getLocalizedName() {
        return localizedName;
    }

    public void setLocalizedName(String localizedName) {
        this.localizedName = localizedName;
    }

    public String getRowHash() {
        return rowHash;
    }

    public void setRowHash(String rowHash) {
        this.rowHash = rowHash;
    }

    public String getLastVersion() {
        return lastVersion;
    }

    public void setLastVersion(String lastVersion) {
        this.lastVersion = lastVersion;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }
}
