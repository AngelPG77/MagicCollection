package pga.magiccollectionspring.card.api.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CardSyncStatusDTO(
        boolean scryfallInSync,
        boolean catalogStateMissing,
        LocalDateTime lastSyncedAt,
        String defaultCardsRemoteToken,
        String allCardsRemoteToken,
        List<LanguageSyncStatusDTO> languages
) {}
