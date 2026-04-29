package pga.magiccollectionspring.card.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public class BulkDataDTO {
    private String id;
    private String type;
    @JsonProperty("updated_at")
    private OffsetDateTime updatedAt;
    private String uri;
    private String name;
    private String description;
    @JsonProperty("download_uri")
    private String downloadUri;
    @JsonProperty("compressed_size")
    private Long compressedSize;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
    public String getUri() { return uri; }
    public void setUri(String uri) { this.uri = uri; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDownloadUri() { return downloadUri; }
    public void setDownloadUri(String downloadUri) { this.downloadUri = downloadUri; }
    public Long getCompressedSize() { return compressedSize; }
    public void setCompressedSize(Long compressedSize) { this.compressedSize = compressedSize; }
}
