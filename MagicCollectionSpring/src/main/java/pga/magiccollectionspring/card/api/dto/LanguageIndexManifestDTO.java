package pga.magiccollectionspring.card.api.dto;

import pga.magiccollectionspring.card.domain.LanguageIndexStatus;

import java.time.LocalDateTime;

public record LanguageIndexManifestDTO(
        String languageCode,
        String version,
        String checksum,
        long totalRows,
        LocalDateTime generatedAt,
        LocalDateTime sourceLastUpdated,
        LanguageIndexStatus status,
        String artifactPath,
        boolean deltaAvailable
) {}
