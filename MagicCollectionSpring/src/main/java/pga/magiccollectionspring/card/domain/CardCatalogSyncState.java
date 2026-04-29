package pga.magiccollectionspring.card.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "card_catalog_sync_state")
public class CardCatalogSyncState {

    @Id
    @Column(name = "bulk_type", nullable = false, length = 64)
    private String bulkType;

    @Column(name = "remote_version_token", nullable = false, length = 128)
    private String remoteVersionToken;

    @Column(name = "last_synced_at", nullable = false)
    private java.time.LocalDateTime lastSyncedAt;

    public String getBulkType() {
        return bulkType;
    }

    public void setBulkType(String bulkType) {
        this.bulkType = bulkType;
    }

    public String getRemoteVersionToken() {
        return remoteVersionToken;
    }

    public void setRemoteVersionToken(String remoteVersionToken) {
        this.remoteVersionToken = remoteVersionToken;
    }

    public java.time.LocalDateTime getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(java.time.LocalDateTime lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }
}
