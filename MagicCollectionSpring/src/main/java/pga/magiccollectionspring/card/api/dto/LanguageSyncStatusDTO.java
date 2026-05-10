package pga.magiccollectionspring.card.api.dto;

import pga.magiccollectionspring.card.domain.LanguageIndexStatus;

public record LanguageSyncStatusDTO(
        String languageCode,
        String version,
        String checksum,
        long totalRows,
        LanguageIndexStatus status
) {}
