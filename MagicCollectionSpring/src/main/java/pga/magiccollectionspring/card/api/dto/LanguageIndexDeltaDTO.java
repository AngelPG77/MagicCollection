package pga.magiccollectionspring.card.api.dto;

import java.util.List;

public record LanguageIndexDeltaDTO(
        String languageCode,
        String fromVersion,
        String targetVersion,
        String checksum,
        long totalRows,
        List<CardLocalizedNameDTO> upserts,
        List<String> deletes
) {}
