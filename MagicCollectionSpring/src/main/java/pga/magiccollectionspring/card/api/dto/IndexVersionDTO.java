package pga.magiccollectionspring.card.api.dto;

import java.time.LocalDateTime;

public record IndexVersionDTO(
    LocalDateTime lastUpdated,
    long totalRows,
    float estimatedSizeMb,
    String version,
    String checksum
) {}
