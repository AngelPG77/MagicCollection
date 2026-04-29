package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "index_language_stage_row", indexes = {
        @Index(name = "idx_index_lang_stage_token_lang", columnList = "build_token, language_code")
})
public class IndexLanguageStageRow {
    @EmbeddedId
    private IndexLanguageStageRowId id;

    @Column(name = "localized_name", nullable = false)
    private String localizedName;

    @Column(name = "row_hash", nullable = false, length = 64)
    private String rowHash;

    public IndexLanguageStageRow() {}

    public IndexLanguageStageRow(IndexLanguageStageRowId id, String localizedName, String rowHash) {
        this.id = id;
        this.localizedName = localizedName;
        this.rowHash = rowHash;
    }

    public IndexLanguageStageRowId getId() {
        return id;
    }

    public void setId(IndexLanguageStageRowId id) {
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
}
