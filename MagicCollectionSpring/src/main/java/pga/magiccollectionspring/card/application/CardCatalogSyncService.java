package pga.magiccollectionspring.card.application;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import pga.magiccollectionspring.card.domain.Card;
import pga.magiccollectionspring.card.domain.CardCatalogSyncState;
import pga.magiccollectionspring.card.domain.CardLocalization;
import pga.magiccollectionspring.card.domain.CardLocalizationId;
import pga.magiccollectionspring.card.domain.ColorMaskCodec;
import pga.magiccollectionspring.card.domain.ICardCatalogSyncStateRepository;
import pga.magiccollectionspring.card.domain.ICardLocalizationRepository;
import pga.magiccollectionspring.card.domain.IMtgSetRepository;
import pga.magiccollectionspring.card.domain.ICardRepository;
import pga.magiccollectionspring.card.api.dto.CardSyncStatusDTO;
import pga.magiccollectionspring.card.api.dto.LanguageIndexManifestDTO;
import pga.magiccollectionspring.card.api.dto.LanguageSyncStatusDTO;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.BulkDataDTO;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
public class CardCatalogSyncService {
    private static final Logger log = LoggerFactory.getLogger(CardCatalogSyncService.class);
    private static final int LOCALIZATION_BATCH_SIZE = 1000;
    private static final int CONNECT_TIMEOUT_MS = 30_000;
    private static final int READ_TIMEOUT_MS = 300_000;
    private static final int ALL_CARDS_MAX_RETRIES = 4;
    long allCardsRetryBaseDelayMs = 5_000L; // package-private for test override via ReflectionTestUtils

    private final AtomicBoolean syncInProgress = new AtomicBoolean(false);

    private final ICardRepository cardRepository;
    private final ICardLocalizationRepository cardLocalizationRepository;
    private final ICardCatalogSyncStateRepository cardCatalogSyncStateRepository;
    private final IMtgSetRepository mtgSetRepository;
    private final ScryfallPort scryfallPort;
    private final ObjectMapper objectMapper;
    private final CardLanguageSupport cardLanguageSupport;
    private final LanguageIndexBuildService languageIndexBuildService;
    private final LanguageIndexAsyncService languageIndexAsyncService;

    public CardCatalogSyncService(ICardRepository cardRepository,
                                  ICardLocalizationRepository cardLocalizationRepository,
                                  ICardCatalogSyncStateRepository cardCatalogSyncStateRepository,
                                  IMtgSetRepository mtgSetRepository,
                                  ScryfallPort scryfallPort,
                                  ObjectMapper objectMapper,
                                  CardLanguageSupport cardLanguageSupport,
                                  LanguageIndexBuildService languageIndexBuildService,
                                  LanguageIndexAsyncService languageIndexAsyncService) {
        this.cardRepository = cardRepository;
        this.cardLocalizationRepository = cardLocalizationRepository;
        this.cardCatalogSyncStateRepository = cardCatalogSyncStateRepository;
        this.mtgSetRepository = mtgSetRepository;
        this.scryfallPort = scryfallPort;
        this.objectMapper = objectMapper;
        this.cardLanguageSupport = cardLanguageSupport;
        this.languageIndexBuildService = languageIndexBuildService;
        this.languageIndexAsyncService = languageIndexAsyncService;
    }

    public void syncFullCatalog() {
        syncFullCatalog(false);
    }

    /**
     * Read-only check that compares the backend's stored Scryfall bulk tokens against the
     * current Scryfall bulk-data tokens, and returns the per-language manifest (version +
     * checksum) so clients can decide whether they need to download anything.
     * <p>
     * Never mutates state. If Scryfall is unreachable, returns scryfallInSync=false so the
     * client falls back to the regular sync flow.
     */
    public CardSyncStatusDTO getSyncStatus(List<String> requestedLanguages) {
        boolean scryfallInSync = false;
        boolean catalogStateMissing = true;
        LocalDateTime lastSyncedAt = null;
        String defaultCardsRemoteToken = null;
        String allCardsRemoteToken = null;

        try {
            List<BulkDataDTO> bulkInfos = scryfallPort.getBulkDataInfo().get();
            BulkDataDTO defaultCards = bulkInfos.stream()
                    .filter(b -> "default_cards".equals(b.getType()))
                    .findFirst()
                    .orElse(null);
            BulkDataDTO allCards = bulkInfos.stream()
                    .filter(b -> "all_cards".equals(b.getType()))
                    .findFirst()
                    .orElse(null);

            defaultCardsRemoteToken = toRemoteVersionToken(defaultCards);
            allCardsRemoteToken = toRemoteVersionToken(allCards);

            Optional<CardCatalogSyncState> defaultState =
                    cardCatalogSyncStateRepository.findByBulkType("default_cards");
            Optional<CardCatalogSyncState> allState =
                    cardCatalogSyncStateRepository.findByBulkType("all_cards");
            catalogStateMissing = defaultState.isEmpty() && allState.isEmpty();

            boolean defaultMatches = defaultCards != null
                    && defaultState.map(s -> Objects.equals(s.getRemoteVersionToken(), toRemoteVersionToken(defaultCards)))
                            .orElse(false);
            boolean allMatches;
            if (allCards == null) {
                // Scryfall didn't expose all_cards; treat as not-relevant rather than mismatch.
                allMatches = true;
            } else {
                allMatches = allState
                        .map(s -> Objects.equals(s.getRemoteVersionToken(), toRemoteVersionToken(allCards)))
                        .orElse(false);
            }

            scryfallInSync = defaultMatches && allMatches;
            lastSyncedAt = defaultState.map(CardCatalogSyncState::getLastSyncedAt)
                    .orElseGet(() -> allState.map(CardCatalogSyncState::getLastSyncedAt).orElse(null));
        } catch (Exception ex) {
            log.warn("No se pudo verificar el estado de sincronización con Scryfall: {}", ex.getMessage());
        }

        List<String> normalizedLanguages = (requestedLanguages == null || requestedLanguages.isEmpty())
                ? cardLanguageSupport.getSupportedLanguages()
                : requestedLanguages.stream()
                        .map(cardLanguageSupport::normalize)
                        .filter(cardLanguageSupport::isSupported)
                        .distinct()
                        .toList();

        List<LanguageSyncStatusDTO> languageStatuses = new ArrayList<>(normalizedLanguages.size());
        for (String lang : normalizedLanguages) {
            try {
                LanguageIndexManifestDTO manifest = languageIndexBuildService.getManifest(lang);
                languageStatuses.add(new LanguageSyncStatusDTO(
                        manifest.languageCode(),
                        manifest.version(),
                        manifest.checksum(),
                        manifest.totalRows(),
                        manifest.status()
                ));
            } catch (Exception ex) {
                log.warn("No se pudo obtener manifest del idioma {}: {}", lang, ex.getMessage());
            }
        }

        return new CardSyncStatusDTO(
                scryfallInSync,
                catalogStateMissing,
                lastSyncedAt,
                defaultCardsRemoteToken,
                allCardsRemoteToken,
                languageStatuses
        );
    }

    public void syncFullCatalog(boolean force) {
        if (!syncInProgress.compareAndSet(false, true)) {
            log.warn("Sincronización completa omitida: ya hay una sincronización en progreso.");
            return;
        }

        log.info("Iniciando sincronización completa del catálogo...");
        syncSets(); // Sincronizar expansiones primero
        final LocalDateTime syncTimestamp = LocalDateTime.now();
        try {
            List<BulkDataDTO> bulkInfos = scryfallPort.getBulkDataInfo().get();
            BulkDataDTO defaultCards = bulkInfos.stream()
                    .filter(b -> "default_cards".equals(b.getType()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No se encontró el archivo de default_cards en Scryfall"));
            BulkDataDTO allCards = bulkInfos.stream()
                    .filter(b -> "all_cards".equals(b.getType()))
                    .findFirst()
                    .orElse(null);

            boolean shouldSyncDefaultCards = shouldSyncBulk(defaultCards, force);
            boolean shouldSyncLocalizations = allCards != null && shouldSyncBulk(allCards, force);

            if (!shouldSyncDefaultCards && !shouldSyncLocalizations) {
                log.info("Sin cambios detectados en Scryfall para default_cards/all_cards. Se omite sincronización completa.");
                return;
            }

            SyncCounters defaultSyncCounters = new SyncCounters(0, 0);
            if (shouldSyncDefaultCards) {
                defaultSyncCounters = syncDefaultCards(defaultCards, syncTimestamp);
                markBulkAsSynced(defaultCards);
            } else {
                log.info("default_cards sin cambios remotos. Se omite descarga del catálogo base.");
            }

            int localizationUpdates = 0;
            if (shouldSyncLocalizations && allCards != null) {
                try {
                    localizationUpdates = syncSupportedLocalizations(syncTimestamp, allCards);
                    markBulkAsSynced(allCards);
                } catch (Exception localizationException) {
                    log.error("Error en sincronización de localizaciones multiidioma. Se continuará sin rebuild: {}",
                            localizationException.getMessage(), localizationException);
                }
            } else if (allCards == null) {
                log.warn("No se encontró el bulk all_cards en Scryfall. Se omite sincronización multiidioma completa.");
            } else {
                log.info("all_cards sin cambios remotos. Se omite descarga de localizaciones.");
            }

            int totalLocalizationUpdates = defaultSyncCounters.updatedLocalizations + localizationUpdates;
            if (defaultSyncCounters.newCards > 0 || totalLocalizationUpdates > 0) {
                languageIndexAsyncService.rebuildAllSupportedLanguagesAsync();
            } else {
                log.info("Sin cambios persistidos en cartas/localizaciones. Se omite rebuild de índices.");
            }
        } catch (Exception e) {
            log.error("Error en syncFullCatalog: {}", e.getMessage(), e);
        } finally {
            syncInProgress.set(false);
        }
    }

    private void syncSets() {
        log.info("Sincronizando expansiones desde Scryfall...");
        try {
            List<pga.magiccollectionspring.card.infrastructure.dto.ScryfallSetDTO> sets = scryfallPort.getSets().get();
            if (sets != null) {
                for (pga.magiccollectionspring.card.infrastructure.dto.ScryfallSetDTO dto : sets) {
                    pga.magiccollectionspring.card.domain.MtgSet set = mtgSetRepository.findByCode(dto.getCode())
                            .orElseGet(() -> {
                                pga.magiccollectionspring.card.domain.MtgSet created = new pga.magiccollectionspring.card.domain.MtgSet();
                                created.setCode(dto.getCode());
                                return created;
                            });
                    set.setName(dto.getName());
                    set.setReleaseDate(dto.getReleasedAt());
                    mtgSetRepository.save(set);
                }
                log.info("Sincronizadas {} expansiones.", sets.size());
            }
        } catch (Exception e) {
            log.error("Error sincronizando expansiones: {}", e.getMessage());
        }
    }

    private SyncCounters syncDefaultCards(BulkDataDTO defaultCards, LocalDateTime syncTimestamp) {
        // OPTIMIZATION: Get all existing IDs at once to avoid thousands of individual existence checks
        Set<String> existingIds = cardRepository.findAllScryfallIds();
        log.info("Cargadas {} IDs existentes para optimizar la sincronización.", existingIds.size());

        Long sizeBytes = defaultCards.getCompressedSize();
        String sizeInfo = sizeBytes != null ? (sizeBytes / 1024 / 1024) + " MB" : "unknown size";
        log.info("Descargando {} ({})...", defaultCards.getName(), sizeInfo);

        RestTemplate restTemplate = createRestTemplate();
        return restTemplate.execute(defaultCards.getDownloadUri(), HttpMethod.GET, null, response -> {
            InputStream is = response.getBody();
            JsonFactory factory = new JsonFactory();
            try (JsonParser parser = factory.createParser(is)) {
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new RuntimeException("Se esperaba un array de cartas");
                }

                int count = 0;
                int skipped = 0;
                int localized = 0;
                Map<CardLocalizationId, CardLocalization> localizationBuffer = new LinkedHashMap<>();

                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    CardScryfallDTO dto = objectMapper.readValue(parser, CardScryfallDTO.class);

                    // Excluir digitales y solo añadir los nuevos
                    if (dto.getName() != null && !dto.getName().startsWith("A-")) {
                        if (!existingIds.contains(dto.getScryfallId())) {
                            sync(dto, syncTimestamp, false);
                            upsertLocalizationInBuffer(dto, syncTimestamp, localizationBuffer);
                            if (localizationBuffer.size() >= LOCALIZATION_BATCH_SIZE) {
                                localized += flushLocalizationBuffer(localizationBuffer);
                            }
                            count++;
                            if (count % 1000 == 0) {
                                log.info(">>> SERVIDOR: Añadidas {} cartas nuevas en la BD MySQL...", count);
                            }
                        } else {
                            skipped++;
                        }
                    }
                }

                localized += flushLocalizationBuffer(localizationBuffer);
                log.info(">>> SERVIDOR: Sincronización finalizada. Nuevas: {}. Omitidas (ya existentes): {}. Localizaciones actualizadas: {}.", count, skipped, localized);
                return new SyncCounters(count, localized);
            }
        });
    }

    public Card sync(CardScryfallDTO dto) {
        return sync(dto, LocalDateTime.now());
    }

    public Card sync(CardScryfallDTO dto, LocalDateTime timestamp) {
        return sync(dto, timestamp, true);
    }

    private Card sync(CardScryfallDTO dto, LocalDateTime timestamp, boolean persistLocalization) {
        Card card = cardRepository.findById(dto.getScryfallId()).orElseGet(() -> {
            Card c = new Card();
            c.setScryfallId(dto.getScryfallId());
            return c;
        });
        card.setOracleId(dto.getOracleId());
        // El name de scryfall es siempre el inglés
        card.setName(dto.getName());
        // El printedName es el nombre en el idioma solicitado (si no es EN)
        if (dto.getPrintedName() != null && !dto.getPrintedName().isBlank()) {
            card.setPrintedName(dto.getPrintedName());
        }
        card.setSetCode(dto.getSetCode() != null ? dto.getSetCode() : "unk");
        card.setOracleText(dto.getOracleText());
        card.setTypeLine(dto.getTypeLine());
        card.setManaCost(dto.getManaCost());
        card.setConvertedManaCost(dto.getCmc() != null ? (int) Math.floor(dto.getCmc()) : null);
        card.setCmc(dto.getCmc() != null ? dto.getCmc().floatValue() : null);
        card.setRarity(dto.getRarity());
        card.setRarityRank(ColorMaskCodec.rarityRank(dto.getRarity()));
        card.setColorMask(ColorMaskCodec.colorMask(dto.getColors(), dto.getManaCost()));
        card.setIdentityMask(ColorMaskCodec.toMask(dto.getColorIdentity()));

        // Manejo de imágenes para cartas normales y de doble cara (DFC)
        String imageUrl = null;
        if (dto.getImageUris() != null) {
            imageUrl = dto.getImageUris().getSmall();
        } else if (dto.getCardFaces() != null && !dto.getCardFaces().isEmpty()) {
            // Si es DFC, la imagen está en la primera cara
            CardScryfallDTO.CardFaceDTO firstFace = dto.getCardFaces().get(0);
            if (firstFace.getImageUris() != null) {
                imageUrl = firstFace.getImageUris().getSmall();
            }
        }
        card.setImageSmallUrl(imageUrl);
        
        card.setLastUpdated(timestamp);

        Card saved = cardRepository.save(card);
        if (persistLocalization) {
            upsertLocalization(dto, timestamp);
        }
        return saved;
    }

    private void upsertLocalization(CardScryfallDTO dto, LocalDateTime timestamp) {
        CardLocalization localization = buildLocalization(dto, timestamp);
        if (localization != null) {
            cardLocalizationRepository.save(localization);
        }
    }

    private void upsertLocalizationInBuffer(CardScryfallDTO dto,
                                            LocalDateTime timestamp,
                                            Map<CardLocalizationId, CardLocalization> buffer) {
        CardLocalization localization = buildLocalization(dto, timestamp);
        if (localization != null) {
            buffer.put(localization.getId(), localization);
        }
    }

    private int flushLocalizationBuffer(Map<CardLocalizationId, CardLocalization> buffer) {
        if (buffer.isEmpty()) {
            return 0;
        }
        List<CardLocalization> pending = new ArrayList<>(buffer.values());
        List<CardLocalizationId> ids = pending.stream()
                .map(CardLocalization::getId)
                .toList();
        Map<CardLocalizationId, CardLocalization> existingById = cardLocalizationRepository.findAllByIds(ids).stream()
                .collect(Collectors.toMap(CardLocalization::getId, item -> item, (left, right) -> left));
        List<CardLocalization> changed = new ArrayList<>();
        for (CardLocalization candidate : pending) {
            CardLocalization existing = existingById.get(candidate.getId());
            if (existing == null || !Objects.equals(existing.getLocalizedName(), candidate.getLocalizedName())) {
                changed.add(candidate);
            }
        }
        changed.forEach(cardLocalizationRepository::save);
        buffer.clear();
        return changed.size();
    }

    private CardLocalization buildLocalization(CardScryfallDTO dto, LocalDateTime timestamp) {
        String oracleId = dto.getOracleId();
        String localizedName = resolveLocalizedName(dto);
        if (oracleId == null || oracleId.isBlank() || localizedName == null || localizedName.isBlank()) {
            return null;
        }

        String languageCode = normalizeLanguage(dto.getLang());
        CardLocalizationId id = new CardLocalizationId(oracleId, languageCode);
        return new CardLocalization(id, localizedName, timestamp);
    }

    private String resolveLocalizedName(CardScryfallDTO dto) {
        if (dto.getPrintedName() != null && !dto.getPrintedName().isBlank()) {
            return dto.getPrintedName();
        }
        return dto.getName();
    }

    private String normalizeLanguage(String lang) {
        return cardLanguageSupport.normalize(lang);
    }

    private int syncSupportedLocalizations(LocalDateTime syncTimestamp, BulkDataDTO allCards) throws Exception {
        Set<String> supportedLanguages = cardLanguageSupport.getSupportedLanguagesSet();
        Long sizeBytes = allCards.getCompressedSize();
        String sizeInfo = sizeBytes != null ? (sizeBytes / 1024 / 1024) + " MB" : "unknown size";
        log.info("Descargando localizaciones desde {} ({})...", allCards.getName(), sizeInfo);

        int attempt = 1;
        while (attempt <= ALL_CARDS_MAX_RETRIES) {
            try {
                return syncSupportedLocalizationsOnce(syncTimestamp, allCards, supportedLanguages);
            } catch (Exception ex) {
                if (!isRetryableBulkDownloadError(ex) || attempt == ALL_CARDS_MAX_RETRIES) {
                    throw ex;
                }
                long delayMs = allCardsRetryBaseDelayMs * attempt;
                log.warn("Fallo descargando/parsing all_cards (intento {}/{}). Reintentando en {} ms. Error: {}",
                        attempt, ALL_CARDS_MAX_RETRIES, delayMs, ex.getMessage());
                sleepSafely(delayMs);
                attempt++;
            }
        }
        return 0;
    }

    private int syncSupportedLocalizationsOnce(
            LocalDateTime syncTimestamp,
            BulkDataDTO allCards,
            Set<String> supportedLanguages
    ) {
        RestTemplate restTemplate = createRestTemplate();
        return restTemplate.execute(allCards.getDownloadUri(), HttpMethod.GET, null, response -> {
            InputStream is = response.getBody();
            JsonFactory factory = new JsonFactory();
            try (JsonParser parser = factory.createParser(is)) {
                if (parser.nextToken() != JsonToken.START_ARRAY) {
                    throw new RuntimeException("Se esperaba un array de cartas para all_cards");
                }

                int localized = 0;
                int skippedUnsupported = 0;
                Map<CardLocalizationId, CardLocalization> localizationBuffer = new LinkedHashMap<>();

                while (parser.nextToken() == JsonToken.START_OBJECT) {
                    CardScryfallDTO dto = objectMapper.readValue(parser, CardScryfallDTO.class);
                    String languageCode = normalizeLanguage(dto.getLang());
                    if (!supportedLanguages.contains(languageCode)) {
                        skippedUnsupported++;
                        continue;
                    }
                    if (dto.getName() != null && dto.getName().startsWith("A-")) {
                        continue;
                    }

                    upsertLocalizationInBuffer(dto, syncTimestamp, localizationBuffer);
                    if (localizationBuffer.size() >= LOCALIZATION_BATCH_SIZE) {
                        localized += flushLocalizationBuffer(localizationBuffer);
                    }
                }
                localized += flushLocalizationBuffer(localizationBuffer);
                log.info("Localizaciones multiidioma sincronizadas: {}. Idiomas no soportados omitidos: {}.", localized, skippedUnsupported);
                return localized;
            }
        });
    }

    private boolean shouldSyncBulk(BulkDataDTO bulkData, boolean force) {
        if (force) {
            return true;
        }
        if (bulkData == null) {
            return false;
        }
        String remoteToken = toRemoteVersionToken(bulkData);
        if (remoteToken == null || remoteToken.isBlank()) {
            return true;
        }
        return cardCatalogSyncStateRepository.findByBulkType(bulkData.getType())
                .map(state -> !remoteToken.equals(state.getRemoteVersionToken()))
                .orElse(true);
    }

    private void markBulkAsSynced(BulkDataDTO bulkData) {
        if (bulkData == null || bulkData.getType() == null || bulkData.getType().isBlank()) {
            return;
        }
        String remoteToken = toRemoteVersionToken(bulkData);
        if (remoteToken == null || remoteToken.isBlank()) {
            return;
        }
        CardCatalogSyncState syncState = cardCatalogSyncStateRepository.findByBulkType(bulkData.getType())
                .orElseGet(() -> {
                    CardCatalogSyncState created = new CardCatalogSyncState();
                    created.setBulkType(bulkData.getType());
                    return created;
                });
        syncState.setRemoteVersionToken(remoteToken);
        syncState.setLastSyncedAt(LocalDateTime.now());
        cardCatalogSyncStateRepository.save(syncState);
    }

    private String toRemoteVersionToken(BulkDataDTO bulkData) {
        if (bulkData == null) {
            return null;
        }
        String id = bulkData.getId();
        String updatedAt = bulkData.getUpdatedAt() != null ? bulkData.getUpdatedAt().toInstant().toString() : null;
        Long compressedSize = bulkData.getCompressedSize();
        String type = bulkData.getType();
        if ((id != null && !id.isBlank()) || (updatedAt != null && !updatedAt.isBlank()) || compressedSize != null) {
            String sizeToken = compressedSize != null ? String.valueOf(compressedSize) : "";
            return String.join("|",
                    type != null ? type : "",
                    id != null ? id : "",
                    updatedAt != null ? updatedAt : "",
                    sizeToken
            );
        }
        return bulkData.getDownloadUri();
    }

    private RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT_MS);
        requestFactory.setReadTimeout(READ_TIMEOUT_MS);
        return new RestTemplate(requestFactory);
    }

    private boolean isRetryableBulkDownloadError(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof ResourceAccessException) {
                return true;
            }
            String message = current.getMessage();
            if (message != null && message.toLowerCase().contains("premature eof")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private void sleepSafely(long delayMs) {
        try {
            TimeUnit.MILLISECONDS.sleep(delayMs);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Reintento interrumpido durante sync de all_cards", interruptedException);
        }
    }

    private static final class SyncCounters {
        private final int newCards;
        private final int updatedLocalizations;

        private SyncCounters(int newCards, int updatedLocalizations) {
            this.newCards = newCards;
            this.updatedLocalizations = updatedLocalizations;
        }
    }
}
