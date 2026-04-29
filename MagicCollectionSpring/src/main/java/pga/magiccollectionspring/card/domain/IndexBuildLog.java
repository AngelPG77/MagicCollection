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
@Table(name = "index_build_log", indexes = {
        @Index(name = "idx_index_build_log_language_started", columnList = "language_code,started_at"),
        @Index(name = "idx_index_build_log_status", columnList = "status")
})
public class IndexBuildLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "language_code", nullable = false, length = 10)
    private String languageCode;

    @Column(name = "version", length = 64)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private LanguageIndexStatus status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column(name = "total_rows")
    private Long totalRows;

    @Column(name = "upserts_count")
    private Long upsertsCount;

    @Column(name = "deletes_count")
    private Long deletesCount;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "build_token", length = 64)
    private String buildToken;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public IndexBuildLog() {}

    public Long getId() {
        return id;
    }

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

    public LanguageIndexStatus getStatus() {
        return status;
    }

    public void setStatus(LanguageIndexStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(LocalDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    public Long getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(Long totalRows) {
        this.totalRows = totalRows;
    }

    public Long getUpsertsCount() {
        return upsertsCount;
    }

    public void setUpsertsCount(Long upsertsCount) {
        this.upsertsCount = upsertsCount;
    }

    public Long getDeletesCount() {
        return deletesCount;
    }

    public void setDeletesCount(Long deletesCount) {
        this.deletesCount = deletesCount;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getBuildToken() {
        return buildToken;
    }

    public void setBuildToken(String buildToken) {
        this.buildToken = buildToken;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
