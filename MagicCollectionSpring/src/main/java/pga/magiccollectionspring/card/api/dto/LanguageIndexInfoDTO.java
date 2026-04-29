package pga.magiccollectionspring.card.api.dto;

import pga.magiccollectionspring.card.domain.LanguageIndexStatus;

import java.time.LocalDateTime;

public record LanguageIndexInfoDTO(
        String languageCode,
        String version,
        String checksum,
        long totalRows,
        LocalDateTime generatedAt,
        LanguageIndexStatus status
) {}
