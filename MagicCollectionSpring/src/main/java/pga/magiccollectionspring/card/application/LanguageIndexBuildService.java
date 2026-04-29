package pga.magiccollectionspring.card.application;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pga.magiccollectionspring.card.api.dto.*;
import pga.magiccollectionspring.card.domain.*;
import pga.magiccollectionspring.card.infrastructure.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LanguageIndexBuildService {
    private static final Logger log = LoggerFactory.getLogger(LanguageIndexBuildService.class);
    private static final int PAGE_SIZE = 2_000;
    private static final int MAX_BUILD_LOG_ITEMS = 50;
    private static final String EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
    private final ICardRepository cardRepository;
    private final ICardLocalizationRepository cardLocalizationRepository;
    private final IndexLanguageStateRepository indexLanguageStateRepository;
    private final IndexLanguageStageRowRepository indexLanguageStageRowRepository;
    private final IndexBuildLogRepository indexBuildLogRepository;
    private final CardLanguageSupport cardLanguageSupport;
    private final MeterRegistry meterRegistry;
    private final LanguageIndexInternalService internalService;
    private final ConcurrentHashMap<String, Object> languageLocks = new ConcurrentHashMap<>();

    public LanguageIndexBuildService(
            ICardRepository cardRepository,
            ICardLocalizationRepository cardLocalizationRepository,
            IndexLanguageStateRepository indexLanguageStateRepository,
            IndexLanguageStageRowRepository indexLanguageStageRowRepository,
            IndexBuildLogRepository indexBuildLogRepository,
            CardLanguageSupport cardLanguageSupport,
            MeterRegistry meterRegistry,
            LanguageIndexInternalService internalService
    ) {
        this.cardRepository = cardRepository;
        this.cardLocalizationRepository = cardLocalizationRepository;
        this.indexLanguageStateRepository = indexLanguageStateRepository;
        this.indexLanguageStageRowRepository = indexLanguageStageRowRepository;
        this.indexBuildLogRepository = indexBuildLogRepository;
        this.cardLanguageSupport = cardLanguageSupport;
        this.meterRegistry = meterRegistry;
        this.internalService = internalService;
    }

    public List<String> getSupportedLanguages() {
        return cardLanguageSupport.getSupportedLanguages();
    }

    public List<LanguageIndexInfoDTO> getLanguages() {
        return getSupportedLanguages().stream()
                .map(languageCode -> indexLanguageStateRepository.findByLanguageCode(languageCode)
                        .map(state -> new LanguageIndexInfoDTO(
                                languageCode,
                                state.getVersion(),
                                state.getChecksum(),
                                state.getTotalRows(),
                                state.getGeneratedAt(),
                                state.getStatus()
                        ))
                        .orElseGet(() -> new LanguageIndexInfoDTO(
                                languageCode,
                                null,
                                null,
                                0L,
                                null,
                                LanguageIndexStatus.BUILDING
                        )))
                .toList();
    }

    public List<IndexBuildLogDTO> getRecentBuildLogs(String languageCode, int limit) {
        String normalized = normalizeSupportedLanguage(languageCode);
        int normalizedLimit = Math.min(Math.max(limit, 1), MAX_BUILD_LOG_ITEMS);
        return indexBuildLogRepository.findByLanguageCodeOrderByStartedAtDesc(
                        normalized,
                        PageRequest.of(0, normalizedLimit)
                ).stream()
                .map(item -> new IndexBuildLogDTO(
                        item.getLanguageCode(),
                        item.getVersion(),
                        item.getStatus(),
                        item.getStartedAt(),
                        item.getFinishedAt(),
                        item.getDurationMs(),
                        item.getTotalRows(),
                        item.getUpsertsCount(),
                        item.getDeletesCount(),
                        item.getChecksum(),
                        item.getErrorMessage()
                ))
                .toList();
    }

    @Transactional
    public LanguageIndexManifestDTO getManifest(String languageCode) {
        String normalized = normalizeSupportedLanguage(languageCode);
        IndexLanguageState state = internalService.getReadyState(normalized);
        if (state == null) {
            return rebuildLanguage(normalized);
        }
        return toManifest(state);
    }

    @Transactional
    public LanguageIndexDeltaDTO getDelta(String languageCode, String sinceVersion) {
        String normalized = normalizeSupportedLanguage(languageCode);
        LanguageIndexManifestDTO manifest = getManifest(normalized);
        long sinceVersionLong = parseVersion(sinceVersion);
        long targetVersionLong = parseVersion(manifest.version());
        if (sinceVersionLong <= 0L || sinceVersionLong >= targetVersionLong) {
            return new LanguageIndexDeltaDTO(
                    normalized,
                    sinceVersion,
                    manifest.version(),
                    manifest.checksum(),
                    manifest.totalRows(),
                    List.of(),
                    List.of()
            );
        }

        List<IndexLanguageDeltaEntry> entries = internalService.getDeltaEntries(normalized, sinceVersionLong, targetVersionLong);
        Map<String, CardLocalizedNameDTO> upsertsByCardId = new LinkedHashMap<>();
        Set<String> deletes = new LinkedHashSet<>();

        for (IndexLanguageDeltaEntry entry : entries) {
            String scryfallId = entry.getScryfallId();
            if (entry.getChangeType() == LanguageIndexDeltaChangeType.UPSERT) {
                deletes.remove(scryfallId);
                upsertsByCardId.put(scryfallId, new CardLocalizedNameDTO(scryfallId, entry.getLocalizedName()));
                continue;
            }
            upsertsByCardId.remove(scryfallId);
            deletes.add(scryfallId);
        }

        List<CardLocalizedNameDTO> upserts = new ArrayList<>(upsertsByCardId.values());
        List<String> deleteList = new ArrayList<>(deletes);
        meterRegistry.counter("magic.index.delta.requests", "language", normalized).increment();
        meterRegistry.summary("magic.index.delta.upserts", "language", normalized).record(upserts.size());
        meterRegistry.summary("magic.index.delta.deletes", "language", normalized).record(deleteList.size());

        return new LanguageIndexDeltaDTO(
                normalized,
                sinceVersion,
                manifest.version(),
                manifest.checksum(),
                manifest.totalRows(),
                upserts,
                deleteList
        );
    }

    public LanguageIndexManifestDTO rebuildLanguage(String languageCode) {
        return rebuildLanguage(languageCode, null);
    }

    public LanguageIndexManifestDTO rebuildLanguage(String languageCode, String forcedVersion) {
        String normalized = normalizeSupportedLanguage(languageCode);
        synchronized (languageLocks.computeIfAbsent(normalized, ignored -> new Object())) {
            long startedNano = System.nanoTime();
            meterRegistry.counter("magic.index.build.started", "language", normalized).increment();
            LocalDateTime startedAt = LocalDateTime.now();

            IndexBuildLog buildLog = new IndexBuildLog();
            buildLog.setLanguageCode(normalized);
            buildLog.setStatus(LanguageIndexStatus.BUILDING);
            buildLog.setStartedAt(startedAt);

            IndexLanguageState state = indexLanguageStateRepository.findByLanguageCode(normalized)
                    .orElseGet(() -> {
                        IndexLanguageState created = new IndexLanguageState();
                        created.setLanguageCode(normalized);
                        return created;
                    });
            initializeRequiredStateFieldsIfMissing(state, startedAt);
            state.setStatus(LanguageIndexStatus.BUILDING);
            
            internalService.startBuild(buildLog, state);

            String buildToken = null;
            try {
                buildToken = UUID.randomUUID().toString().replace("-", "");
                buildLog.setBuildToken(buildToken);
                BuildState computed = computeState(normalized, buildToken);
                String previousVersion = state.getVersion();
                boolean contentChanged = hasContentChanged(state, computed);
                
                String targetVersion = forcedVersion;
                if (targetVersion == null) {
                    targetVersion = previousVersion;
                    if (targetVersion == null || targetVersion.isBlank() || contentChanged) {
                        targetVersion = nextVersion(previousVersion);
                    }
                }

                LanguageIndexInternalService.DiffResult diff;
                if (contentChanged || !Objects.equals(previousVersion, targetVersion)) {
                    long targetVersionLong = parseVersion(targetVersion);
                    diff = internalService.applyStageAndPublishDelta(normalized, buildToken, targetVersion, targetVersionLong);
                    state.setChecksum(computed.checksum);
                    state.setTotalRows(computed.totalRows);
                    state.setGeneratedAt(computed.generatedAt);
                    state.setSourceLastUpdated(computed.sourceLastUpdated);
                } else {
                    log.info("Índice de idioma {} sin cambios de contenido; se mantiene versión {}", normalized, targetVersion);
                    diff = new LanguageIndexInternalService.DiffResult(0L, 0L);
                    if (state.getChecksum() == null || state.getChecksum().isBlank()) {
                        state.setChecksum(computed.checksum);
                    }
                    if (state.getGeneratedAt() == null) {
                        state.setGeneratedAt(computed.generatedAt);
                    }
                    if (state.getSourceLastUpdated() == null) {
                        state.setSourceLastUpdated(computed.sourceLastUpdated);
                    }
                }

                state.setVersion(targetVersion);
                state.setStatus(LanguageIndexStatus.READY);
                finalizeBuildLogSuccess(buildLog, targetVersion, computed, diff, startedNano);
                
                internalService.finalizeBuild(state, buildLog, buildToken, normalized);
                recordBuildMetrics(normalized, true, computed.totalRows, diff.upsertsCount(), diff.deletesCount(), startedNano);
                return toManifest(state);
            } catch (Exception ex) {
                log.error("Failed to rebuild language index for {}", normalized, ex);
                finalizeBuildLogFailure(buildLog, ex, startedNano);
                internalService.markAsFailed(state, buildLog, buildToken, normalized);
                recordBuildMetrics(normalized, false, 0L, 0L, 0L, startedNano);
                throw ex;
            }
        }
    }

    private BuildState computeState(String languageCode, String buildToken) {
        MessageDigest digest = newSha256();
        long totalRows = 0L;
        int page = 0;
        List<IndexLanguageStageRow> stageRowsBuffer = new ArrayList<>(PAGE_SIZE);
        Slice<CardIndexView> slice;
        do {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, org.springframework.data.domain.Sort.by("scryfallId").ascending());
            slice = cardRepository.findIndexSlice(pageable);
            Set<String> oracleIds = slice.getContent().stream()
                    .map(CardIndexView::getOracleId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<String, String> localizedByOracle = resolveLocalizedNamesByOracle(languageCode, oracleIds);
            for (CardIndexView row : slice.getContent()) {
                String oracleId = row.getOracleId();
                String resolvedName = (oracleId != null)
                        ? localizedByOracle.getOrDefault(oracleId, row.getName())
                        : row.getName();
                String cardRecord = row.getScryfallId() + "|" + resolvedName;
                digest.update((cardRecord + "\n").getBytes(StandardCharsets.UTF_8));
                stageRowsBuffer.add(new IndexLanguageStageRow(
                        new IndexLanguageStageRowId(buildToken, languageCode, row.getScryfallId()),
                        resolvedName,
                        hashRow(cardRecord)
                ));
                if (stageRowsBuffer.size() >= PAGE_SIZE) {
                    indexLanguageStageRowRepository.saveAll(stageRowsBuffer);
                    stageRowsBuffer.clear();
                }
                totalRows++;
            }
            page++;
        } while (slice.hasNext());
        if (!stageRowsBuffer.isEmpty()) {
            indexLanguageStageRowRepository.saveAll(stageRowsBuffer);
            stageRowsBuffer.clear();
        }

        LocalDateTime sourceLastUpdated = maxDate(
                cardRepository.findMaxLastUpdated(),
                cardLocalizationRepository.findMaxLastUpdatedByLanguage(languageCode),
                cardLocalizationRepository.findMaxLastUpdated()
        );
        return new BuildState(
                toHex(digest.digest()),
                totalRows,
                LocalDateTime.now(),
                sourceLastUpdated
        );
    }

    private void finalizeBuildLogSuccess(
            IndexBuildLog buildLog,
            String version,
            BuildState computed,
            LanguageIndexInternalService.DiffResult diffResult,
            long startedNano
    ) {
        long durationMs = nanosToMillis(System.nanoTime() - startedNano);
        buildLog.setStatus(LanguageIndexStatus.READY);
        buildLog.setVersion(version);
        buildLog.setFinishedAt(LocalDateTime.now());
        buildLog.setDurationMs(durationMs);
        buildLog.setTotalRows(computed.totalRows);
        buildLog.setUpsertsCount(diffResult.upsertsCount());
        buildLog.setDeletesCount(diffResult.deletesCount());
        buildLog.setChecksum(computed.checksum);
        buildLog.setErrorMessage(null);
    }

    private void finalizeBuildLogFailure(IndexBuildLog buildLog, Exception ex, long startedNano) {
        long durationMs = nanosToMillis(System.nanoTime() - startedNano);
        buildLog.setStatus(LanguageIndexStatus.FAILED);
        buildLog.setFinishedAt(LocalDateTime.now());
        buildLog.setDurationMs(durationMs);
        buildLog.setErrorMessage(ex.getMessage());
        indexBuildLogRepository.save(buildLog);
    }

    private void recordBuildMetrics(
            String languageCode,
            boolean success,
            long totalRows,
            long upsertsCount,
            long deletesCount,
            long startedNano
    ) {
        long durationMs = nanosToMillis(System.nanoTime() - startedNano);
        meterRegistry.timer("magic.index.build.duration", "language", languageCode, "success", String.valueOf(success))
                .record(durationMs, TimeUnit.MILLISECONDS);
        meterRegistry.counter("magic.index.build.completed", "language", languageCode, "success", String.valueOf(success))
                .increment();
        if (success) {
            meterRegistry.summary("magic.index.build.rows", "language", languageCode).record(totalRows);
            meterRegistry.summary("magic.index.build.upserts", "language", languageCode).record(upsertsCount);
            meterRegistry.summary("magic.index.build.deletes", "language", languageCode).record(deletesCount);
        }
    }

    private static long nanosToMillis(long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    private Map<String, String> resolveLocalizedNamesByOracle(String languageCode, Set<String> oracleIds) {
        if (oracleIds.isEmpty() || "en".equals(languageCode)) {
            return Map.of();
        }
        List<CardLocalization> localizations = cardLocalizationRepository.findByLanguageCodeAndOracleIds(languageCode, oracleIds);
        Map<String, String> localizedByOracle = new HashMap<>();
        for (CardLocalization localization : localizations) {
            localizedByOracle.put(localization.getId().getOracleId(), localization.getLocalizedName());
        }
        return localizedByOracle;
    }

    private String normalizeSupportedLanguage(String languageCode) {
        String normalized = cardLanguageSupport.normalize(languageCode);
        if (!cardLanguageSupport.isSupported(normalized)) {
            throw new IllegalArgumentException("Unsupported language: " + languageCode);
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private LanguageIndexManifestDTO toManifest(IndexLanguageState state) {
        return new LanguageIndexManifestDTO(
                state.getLanguageCode(),
                state.getVersion(),
                state.getChecksum(),
                state.getTotalRows(),
                state.getGeneratedAt(),
                state.getSourceLastUpdated(),
                state.getStatus(),
                state.getArtifactPath(),
                true
        );
    }

    private static MessageDigest newSha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private static String hashRow(String payload) {
        MessageDigest digest = newSha256();
        digest.update(payload.getBytes(StandardCharsets.UTF_8));
        return toHex(digest.digest());
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static long parseVersion(String version) {
        if (version == null || version.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(version);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private static String nextVersion(String currentVersion) {
        long now = Instant.now().toEpochMilli();
        long current = parseVersion(currentVersion);
        return String.valueOf(Math.max(now, current + 1));
    }

    private static boolean hasContentChanged(IndexLanguageState state, BuildState computed) {
        if (state.getStatus() != LanguageIndexStatus.READY) {
            return true;
        }
        if (!Objects.equals(state.getChecksum(), computed.checksum)) {
            return true;
        }
        return state.getTotalRows() != computed.totalRows;
    }

    private static void initializeRequiredStateFieldsIfMissing(IndexLanguageState state, LocalDateTime now) {
        if (state.getVersion() == null || state.getVersion().isBlank()) {
            state.setVersion("0");
        }
        if (state.getChecksum() == null || state.getChecksum().isBlank()) {
            state.setChecksum(EMPTY_SHA256);
        }
        if (state.getGeneratedAt() == null) {
            state.setGeneratedAt(now);
        }
    }

    private static LocalDateTime maxDate(LocalDateTime... values) {
        LocalDateTime result = null;
        for (LocalDateTime value : values) {
            if (value == null) {
                continue;
            }
            if (result == null || value.isAfter(result)) {
                result = value;
            }
        }
        return result;
    }

    private static final class BuildState {
        private final String checksum;
        private final long totalRows;
        private final LocalDateTime generatedAt;
        private final LocalDateTime sourceLastUpdated;

        private BuildState(
                String checksum,
                long totalRows,
                LocalDateTime generatedAt,
                LocalDateTime sourceLastUpdated
        ) {
            this.checksum = checksum;
            this.totalRows = totalRows;
            this.generatedAt = generatedAt;
            this.sourceLastUpdated = sourceLastUpdated;
        }
    }
}
