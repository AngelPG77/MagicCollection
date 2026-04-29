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

    // Ejecutar cada 24 horas. Inicialmente 10 segundos después del arranque.
    @Scheduled(fixedRate = 86400000, initialDelay = 10000)
    public void syncDaily() {
        log.info("Iniciando tarea programada de sincronización con Scryfall...");
        syncService.syncFullCatalog();
    }
}
