package pga.magiccollectionspring.card.api;

import pga.magiccollectionspring.card.application.query.GetAllCards.GetAllCardsQuery;
import pga.magiccollectionspring.card.application.query.GetAllCards.GetAllCardsService;
import pga.magiccollectionspring.card.application.query.GetCardById.GetCardByIdQuery;
import pga.magiccollectionspring.card.application.query.GetCardById.GetCardByIdService;
import pga.magiccollectionspring.card.application.query.GetRandomCard.GetRandomCardQuery;
import pga.magiccollectionspring.card.application.query.GetRandomCard.GetRandomCardService;
import pga.magiccollectionspring.card.application.query.SearchCards.SearchCardsQuery;
import pga.magiccollectionspring.card.application.query.SearchCards.SearchCardsService;
import pga.magiccollectionspring.card.application.CardLanguageSupport;
import pga.magiccollectionspring.card.application.CardCatalogSyncService;
import pga.magiccollectionspring.card.application.LanguageIndexBuildService;
import pga.magiccollectionspring.card.application.LanguageIndexAsyncService;
import pga.magiccollectionspring.card.api.dto.*;
import pga.magiccollectionspring.card.domain.*;
import pga.magiccollectionspring.card.domain.port.ScryfallPort;
import pga.magiccollectionspring.card.infrastructure.dto.CardScryfallDTO;
import pga.magiccollectionspring.shared.exception.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/cards")
public class CardController {
    private static final Logger log = LoggerFactory.getLogger(CardController.class);

    private final SearchCardsService searchCardsService;
    private final GetAllCardsService getAllCardsService;
    private final GetCardByIdService getCardByIdService;
    private final GetRandomCardService getRandomCardService;
    private final ScryfallPort scryfallPort;
    private final ICardRepository cardRepository;
    private final ICardLocalizationRepository cardLocalizationRepository;
    private final IMtgSetRepository mtgSetRepository;
    private final CardCatalogSyncService cardCatalogSyncService;
    private final LanguageIndexBuildService languageIndexBuildService;
    private final LanguageIndexAsyncService languageIndexAsyncService;
    private final CardLanguageSupport cardLanguageSupport;
    private final CardMapper cardMapper;
    private final ObjectMapper objectMapper;

    public CardController(SearchCardsService searchCardsService,
                          GetAllCardsService getAllCardsService,
                          GetCardByIdService getCardByIdService,
                          GetRandomCardService getRandomCardService,
                          ScryfallPort scryfallPort,
                          ICardRepository cardRepository,
                          ICardLocalizationRepository cardLocalizationRepository,
                          IMtgSetRepository mtgSetRepository,
                          CardCatalogSyncService cardCatalogSyncService,
                          LanguageIndexBuildService languageIndexBuildService,
                          LanguageIndexAsyncService languageIndexAsyncService,
                          CardLanguageSupport cardLanguageSupport,
                          CardMapper cardMapper,
                          ObjectMapper objectMapper) {
        this.searchCardsService = searchCardsService;
        this.getAllCardsService = getAllCardsService;
        this.getCardByIdService = getCardByIdService;
        this.getRandomCardService = getRandomCardService;
        this.scryfallPort = scryfallPort;
        this.cardRepository = cardRepository;
        this.cardLocalizationRepository = cardLocalizationRepository;
        this.mtgSetRepository = mtgSetRepository;
        this.cardCatalogSyncService = cardCatalogSyncService;
        this.languageIndexBuildService = languageIndexBuildService;
        this.languageIndexAsyncService = languageIndexAsyncService;
        this.cardLanguageSupport = cardLanguageSupport;
        this.cardMapper = cardMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/search")
    public CompletableFuture<ResponseEntity<CardDTO>> getCardByName(@RequestParam String name, @RequestParam(required = false) String lang) {
        return scryfallPort.findCardByName(name, lang).thenApply(optDto -> {
            CardScryfallDTO dto = optDto
                    .orElseThrow(() -> new ResourceNotFoundException("Carta", name));

            cardCatalogSyncService.sync(dto);

            return ResponseEntity.ok(cardMapper.map(dto));
        });
    }

    @PostMapping("/sync-full")
    public ResponseEntity<Void> syncFullCatalog() {
        CompletableFuture.runAsync(() -> cardCatalogSyncService.syncFullCatalog(true));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/library")
    public ResponseEntity<List<CardDTO>> getAllKnownCards() {
        return ResponseEntity.ok(getAllCardsService.execute(new GetAllCardsQuery()).cards());
    }

    @GetMapping("/index/sync-status")
    public ResponseEntity<CardSyncStatusDTO> getSyncStatus(
            @RequestParam(name = "langs", required = false) String langs) {
        List<String> requestedLanguages = (langs == null || langs.isBlank())
                ? List.of()
                : java.util.Arrays.stream(langs.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList();
        return ResponseEntity.ok(cardCatalogSyncService.getSyncStatus(requestedLanguages));
    }

    @GetMapping("/index/version")
    public ResponseEntity<IndexVersionDTO> getIndexVersion() {
        LanguageIndexManifestDTO enManifest = languageIndexBuildService.getManifest("en");
        
        return ResponseEntity.ok(new IndexVersionDTO(
                enManifest.generatedAt(),
                enManifest.totalRows(),
                (enManifest.totalRows() * 300f) / 1024f / 1024f,
                enManifest.version(),
                enManifest.checksum()
        ));
    }

    @GetMapping("/sets")
    public ResponseEntity<List<MtgSet>> getSets() {
        return ResponseEntity.ok(mtgSetRepository.findAllByOrderByReleaseDateDesc());
    }

    @GetMapping("/index/{lang}/page")
    public ResponseEntity<CardMetadataPageDTO> getIndexPageForLanguage(
            @PathVariable String lang,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "1000") int limit) {
        long startedAt = System.nanoTime();
        String normalizedLang = normalizeLanguage(lang);
        int normalizedOffset = Math.max(offset, 0);
        int normalizedLimit = Math.min(Math.max(limit, 100), 2000);

        int pageNumber = normalizedOffset / normalizedLimit;
        PageRequest pageRequest = PageRequest.of(
                pageNumber,
                normalizedLimit,
                Sort.by(Sort.Direction.ASC, "name")
        );

        Page<CardIndexView> page = cardRepository.findIndexPage(pageRequest);
        List<CardIndexView> cards = page.getContent();
        Set<String> oracleIds = cards.stream()
                .map(CardIndexView::getOracleId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
        Map<String, String> localizedByOracle = loadLocalizedNamesForOracleIds(normalizedLang, oracleIds);

        List<CardMetadataDTO> items = cards.stream()
                .map(card -> {
                    String localizedName = card.getOracleId() != null ? localizedByOracle.get(card.getOracleId()) : null;
                    return cardMapper.mapMetadata(card, normalizedLang, localizedName);
                })
                .toList();

        int responseOffset = pageNumber * normalizedLimit;
        CardMetadataPageDTO response = new CardMetadataPageDTO(
                items,
                responseOffset,
                normalizedLimit,
                page.hasNext(),
                page.getTotalElements()
        );
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        log.info("cards.index.page lang={} offset={} limit={} items={} total={} hasMore={} elapsedMs={}", normalizedLang, responseOffset, normalizedLimit, items.size(), page.getTotalElements(), page.hasNext(), elapsedMs);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/index/languages")
    public ResponseEntity<List<LanguageIndexInfoDTO>> getSupportedIndexLanguages() {
        return ResponseEntity.ok(languageIndexBuildService.getLanguages());
    }

    @GetMapping("/index/{lang}/manifest")
    public ResponseEntity<LanguageIndexManifestDTO> getLanguageManifest(@PathVariable String lang) {
        return ResponseEntity.ok(languageIndexBuildService.getManifest(lang));
    }

    @GetMapping("/index/{lang}/delta")
    public ResponseEntity<LanguageIndexDeltaDTO> getLanguageDelta(
            @PathVariable String lang,
            @RequestParam(name = "sinceVersion") String sinceVersion
    ) {
        return ResponseEntity.ok(languageIndexBuildService.getDelta(lang, sinceVersion));
    }

    @GetMapping("/index/{lang}/builds")
    public ResponseEntity<List<IndexBuildLogDTO>> getLanguageBuildLogs(
            @PathVariable String lang,
            @RequestParam(defaultValue = "20") int limit
    ) {
        return ResponseEntity.ok(languageIndexBuildService.getRecentBuildLogs(lang, limit));
    }

    @PostMapping("/index/rebuild/{lang}")
    public ResponseEntity<Void> rebuildLanguageIndex(@PathVariable String lang) {
        languageIndexAsyncService.rebuildLanguageAsync(lang);
        return ResponseEntity.accepted().build();
    }

    @GetMapping({"/index/{lang}/snapshot", "/index/{lang}/names/snapshot"})
    public StreamingResponseBody getLocalizedNamesSnapshot(
            @PathVariable String lang,
            HttpServletResponse response) {
        String normalizedLang = normalizeLanguage(lang);
        LanguageIndexManifestDTO manifest = languageIndexBuildService.getManifest(normalizedLang);
        int pageSize = 2000;
        long startedAt = System.nanoTime();
        long totalCards = manifest.totalRows();
        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setHeader("X-Total-Cards", String.valueOf(totalCards));
        response.setHeader("X-Index-Version", manifest.version());
        response.setHeader("X-Index-Checksum", manifest.checksum());

        return outputStream -> {
            try (JsonGenerator jsonGenerator = objectMapper.getFactory().createGenerator(outputStream)) {
                jsonGenerator.writeStartArray();
                int pageNumber = 0;
                long emitted = 0L;

                while (true) {
                    PageRequest pageRequest = PageRequest.of(
                            pageNumber,
                            pageSize,
                            Sort.by(Sort.Direction.ASC, "scryfallId")
                    );
                    Slice<CardIndexView> slice =
                            cardRepository.findIndexSlice(pageRequest);
                    List<CardIndexView> cards = slice.getContent();
                    if (cards.isEmpty()) {
                        break;
                    }

                    Set<String> oracleIds = cards.stream()
                            .map(CardIndexView::getOracleId)
                            .filter(id -> id != null && !id.isBlank())
                            .collect(Collectors.toSet());
                    Map<String, String> localizedByOracle = loadLocalizedNamesForOracleIds(normalizedLang, oracleIds);

                    for (CardIndexView card : cards) {
                        String localizedName = card.getName();
                        if (!"en".equals(normalizedLang) && card.getOracleId() != null) {
                            localizedName = localizedByOracle.getOrDefault(card.getOracleId(), card.getName());
                        }
                        CardLocalizedNameDTO dto = new CardLocalizedNameDTO(card.getScryfallId(), localizedName);
                        jsonGenerator.writeObject(dto);
                        emitted++;
                    }
                    jsonGenerator.flush();

                    if (!slice.hasNext()) {
                        break;
                    }
                    pageNumber++;
                }

                jsonGenerator.writeEndArray();
                jsonGenerator.flush();
                long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
                log.info("cards.index.names.snapshot lang={} emitted={} total={} elapsedMs={}", normalizedLang, emitted, totalCards, elapsedMs);
            }
        };
    }

    @GetMapping("/discover")
    public CompletableFuture<ResponseEntity<?>> discoverCards(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String colors,
            @RequestParam(defaultValue = "false") Boolean colorIdentity,
            @RequestParam(required = false) String colorLogic,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String text,
            @RequestParam(required = false) String manaCost,
            @RequestParam(required = false) String set,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String artist,
            @RequestParam(required = false) String lang) {
        
        SearchCardsQuery searchCardsQuery = new SearchCardsQuery(
                query, colors, colorIdentity, colorLogic, type, text, manaCost, set, rarity, artist, lang
        );
        
        return searchCardsService.execute(searchCardsQuery)
                .thenApply(response -> ResponseEntity.ok(response.cards()));
    }

    @GetMapping("/autocomplete")
    public CompletableFuture<ResponseEntity<List<CardSuggestionDTO>>> autocomplete(@RequestParam String query) {
        return scryfallPort.autocomplete(query).thenApply(ResponseEntity::ok);
    }

    @GetMapping("/random")
    public CompletableFuture<ResponseEntity<CardDTO>> getRandomCard() {
        return getRandomCardService.execute(new GetRandomCardQuery())
                .thenApply(response -> ResponseEntity.ok(response.card()));
    }

    @GetMapping("/scryfall/{id}")
    public CompletableFuture<ResponseEntity<CardDTO>> getCardByScryfallId(@PathVariable String id, @RequestParam(required = false) String lang) {
        return scryfallPort.findCardByScryfallId(id, lang).thenApply(optDto -> {
            CardScryfallDTO dto = optDto
                    .orElseThrow(() -> new ResourceNotFoundException("Carta", id));

            cardCatalogSyncService.sync(dto);

            return ResponseEntity.ok(cardMapper.map(dto));
        });
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardDTO> getCardById(@PathVariable String id) {
        return ResponseEntity.ok(getCardByIdService.execute(new GetCardByIdQuery(id)).card());
    }

    private Map<String, String> loadLocalizedNamesForOracleIds(String normalizedLang, Set<String> oracleIds) {
        if ("en".equals(normalizedLang) || oracleIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return cardLocalizationRepository.findByLanguageCodeAndOracleIds(normalizedLang, oracleIds).stream()
                .filter(loc -> loc.getId() != null && loc.getId().getOracleId() != null && !loc.getId().getOracleId().isBlank())
                .collect(Collectors.toMap(
                        loc -> loc.getId().getOracleId(),
                        CardLocalization::getLocalizedName,
                        (left, right) -> left
                ));
    }

    private String normalizeLanguage(String lang) {
        String normalized = cardLanguageSupport.normalize(lang);
        if (!cardLanguageSupport.isSupported(normalized)) {
            throw new IllegalArgumentException("Unsupported language: " + lang);
        }
        return normalized;
    }

    private LocalDateTime maxDate(LocalDateTime first, LocalDateTime second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.isAfter(second) ? first : second;
    }
}
