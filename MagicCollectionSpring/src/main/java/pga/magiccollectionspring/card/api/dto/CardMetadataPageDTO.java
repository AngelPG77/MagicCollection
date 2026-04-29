package pga.magiccollectionspring.card.api.dto;

import java.util.List;

public record CardMetadataPageDTO(
        List<CardMetadataDTO> items,
        int offset,
        int limit,
        boolean hasMore,
        long totalCards
) {
}
