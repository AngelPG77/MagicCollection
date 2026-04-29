package pga.magiccollectionspring.card.api.dto;

import pga.magiccollectionspring.card.domain.LanguageIndexStatus;

import java.time.LocalDateTime;

public record IndexBuildLogDTO(
        String languageCode,
        String version,
        LanguageIndexStatus status,
        LocalDateTime startedAt,
        LocalDateTime finishedAt,
        Long durationMs,
        Long totalRows,
        Long upsertsCount,
        Long deletesCount,
        String checksum,
        String errorMessage
) {}
