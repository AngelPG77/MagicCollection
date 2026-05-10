package pga.magiccollectionspring.card.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CardSyncScheduledService {
    private static final Logger log = LoggerFactory.getLogger(CardSyncScheduledService.class);
    private final CardCatalogSyncService syncService;

    public CardSyncScheduledService(CardCatalogSyncService syncService) {
        this.syncService = syncService;
    }

    // Execute every 24 hours. Initially 10 seconds after startup.
    @Scheduled(fixedRate = 86400000, initialDelay = 10000)
    public void syncDaily() {
        log.info("[SCHEDULED-TASK] Starting daily synchronization with Scryfall...");
        try {
            syncService.syncFullCatalog();
            log.info("[SCHEDULED-TASK] Daily synchronization finished successfully.");
        } catch (Exception e) {
            log.error("[SCHEDULED-TASK] Daily synchronization failed: {}", e.getMessage(), e);
        }
    }
}
