package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "index_language_state")
public class IndexLanguageState {

    @Id
    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "version", nullable = false, length = 64)
    private String version;

    @Column(name = "checksum", nullable = false, length = 64)
    private String checksum;

    @Column(name = "total_rows", nullable = false)
    private long totalRows;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;

    @Column(name = "source_last_updated")
    private LocalDateTime sourceLastUpdated;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private LanguageIndexStatus status;

    @Column(name = "artifact_path")
    private String artifactPath;

    public IndexLanguageState() {}

    public String getLanguageCode() {
        return languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(long totalRows) {
        this.totalRows = totalRows;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public LocalDateTime getSourceLastUpdated() {
        return sourceLastUpdated;
    }

    public void setSourceLastUpdated(LocalDateTime sourceLastUpdated) {
        this.sourceLastUpdated = sourceLastUpdated;
    }

    public LanguageIndexStatus getStatus() {
        return status;
    }

    public void setStatus(LanguageIndexStatus status) {
        this.status = status;
    }

    public String getArtifactPath() {
        return artifactPath;
    }

    public void setArtifactPath(String artifactPath) {
        this.artifactPath = artifactPath;
    }
}
