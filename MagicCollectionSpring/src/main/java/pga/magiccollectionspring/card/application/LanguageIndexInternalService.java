package pga.magiccollectionspring.card.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pga.magiccollectionspring.card.api.dto.LanguageIndexManifestDTO;
import pga.magiccollectionspring.card.domain.*;
import pga.magiccollectionspring.card.infrastructure.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LanguageIndexInternalService {
    private static final Logger log = LoggerFactory.getLogger(LanguageIndexInternalService.class);
    private static final int PAGE_SIZE = 2_000;

    private final IndexLanguageStateRepository indexLanguageStateRepository;
    private final IndexBuildLogRepository indexBuildLogRepository;
    private final IndexLanguageRowStateRepository indexLanguageRowStateRepository;
    private final IndexLanguageDeltaEntryRepository indexLanguageDeltaEntryRepository;
    private final IndexLanguageStageRowRepository indexLanguageStageRowRepository;

    public LanguageIndexInternalService(
            IndexLanguageStateRepository indexLanguageStateRepository,
            IndexBuildLogRepository indexBuildLogRepository,
            IndexLanguageRowStateRepository indexLanguageRowStateRepository,
            IndexLanguageDeltaEntryRepository indexLanguageDeltaEntryRepository,
            IndexLanguageStageRowRepository indexLanguageStageRowRepository
    ) {
        this.indexLanguageStateRepository = indexLanguageStateRepository;
        this.indexBuildLogRepository = indexBuildLogRepository;
        this.indexLanguageRowStateRepository = indexLanguageRowStateRepository;
        this.indexLanguageDeltaEntryRepository = indexLanguageDeltaEntryRepository;
        this.indexLanguageStageRowRepository = indexLanguageStageRowRepository;
    }

    @Transactional
    public IndexLanguageState getReadyState(String languageCode) {
        return indexLanguageStateRepository.findByLanguageCode(languageCode)
                .filter(state -> state.getStatus() == LanguageIndexStatus.READY)
                .orElse(null);
    }

    @Transactional
    public void startBuild(IndexBuildLog buildLog, IndexLanguageState state) {
        indexBuildLogRepository.save(buildLog);
        indexLanguageStateRepository.save(state);
    }

    @Transactional
    public DiffResult applyStageAndPublishDelta(String languageCode, String buildToken, String targetVersion, long targetVersionLong) {
        List<IndexLanguageStageRow> stageRows = indexLanguageStageRowRepository.findByIdBuildTokenAndIdLanguageCode(buildToken, languageCode);
        List<IndexLanguageRowState> existingRows = indexLanguageRowStateRepository.findByIdLanguageCode(languageCode);
        Map<String, IndexLanguageRowState> existingByCardId = existingRows.stream()
                .collect(Collectors.toMap(row -> row.getId().getScryfallId(), row -> row, (left, right) -> left));
        LocalDateTime now = LocalDateTime.now();

        List<IndexLanguageRowState> rowsToSave = new ArrayList<>();
        List<IndexLanguageDeltaEntry> deltaEntries = new ArrayList<>();
        long upsertsCount = 0L;
        long deletesCount = 0L;

        Set<String> stagedCardIds = new HashSet<>();
        for (IndexLanguageStageRow stageRow : stageRows) {
            String scryfallId = stageRow.getId().getScryfallId();
            stagedCardIds.add(scryfallId);
            IndexLanguageRowState existing = existingByCardId.get(scryfallId);
            boolean changed = existing == null ||
                    existing.isDeleted() ||
                    !Objects.equals(existing.getRowHash(), stageRow.getRowHash());

            IndexLanguageRowState targetRow = existing;
            if (targetRow == null) {
                targetRow = new IndexLanguageRowState();
                targetRow.setId(new IndexLanguageRowStateId(languageCode, scryfallId));
            }
            targetRow.setLocalizedName(stageRow.getLocalizedName());
            targetRow.setRowHash(stageRow.getRowHash());
            targetRow.setLastVersion(targetVersion);
            targetRow.setDeleted(false);
            targetRow.setDeletedAt(null);
            targetRow.setUpdatedAt(now);
            if (existing == null || changed) {
                rowsToSave.add(targetRow);
            }

            if (changed) {
                deltaEntries.add(buildDeltaEntry(
                        languageCode,
                        targetVersion,
                        targetVersionLong,
                        scryfallId,
                        LanguageIndexDeltaChangeType.UPSERT,
                        stageRow.getLocalizedName(),
                        now
                ));
                upsertsCount++;
            }
        }

        for (IndexLanguageRowState existing : existingRows) {
            String scryfallId = existing.getId().getScryfallId();
            if (existing.isDeleted() || stagedCardIds.contains(scryfallId)) {
                continue;
            }
            existing.setDeleted(true);
            existing.setDeletedAt(now);
            existing.setUpdatedAt(now);
            existing.setLastVersion(targetVersion);
            rowsToSave.add(existing);
            deltaEntries.add(buildDeltaEntry(
                    languageCode,
                    targetVersion,
                    targetVersionLong,
                    scryfallId,
                    LanguageIndexDeltaChangeType.DELETE,
                    null,
                    now
            ));
            deletesCount++;
        }

        saveRowStatesInBatches(rowsToSave);
        saveDeltaEntriesInBatches(deltaEntries);
        return new DiffResult(upsertsCount, deletesCount);
    }

    @Transactional
    public void finalizeBuild(IndexLanguageState state, IndexBuildLog buildLog, String buildToken, String normalized) {
        indexLanguageStateRepository.save(state);
        indexBuildLogRepository.save(buildLog);
        if (buildToken != null) {
            indexLanguageStageRowRepository.deleteByIdBuildTokenAndIdLanguageCode(buildToken, normalized);
        }
    }

    @Transactional
    public List<IndexLanguageDeltaEntry> getDeltaEntries(String languageCode, long sinceVersionLong, long targetVersionLong) {
        return indexLanguageDeltaEntryRepository
                .findByLanguageCodeAndTargetVersionLongGreaterThanAndTargetVersionLongLessThanEqualOrderByTargetVersionLongAscIdAsc(
                        languageCode,
                        sinceVersionLong,
                        targetVersionLong
                );
    }

    @Transactional
    public void markAsFailed(IndexLanguageState state, IndexBuildLog buildLog, String buildToken, String normalized) {
        state.setStatus(LanguageIndexStatus.FAILED);
        indexLanguageStateRepository.save(state);
        indexBuildLogRepository.save(buildLog);
        if (buildToken != null) {
            indexLanguageStageRowRepository.deleteByIdBuildTokenAndIdLanguageCode(buildToken, normalized);
        }
    }

    private static IndexLanguageDeltaEntry buildDeltaEntry(
            String languageCode,
            String targetVersion,
            long targetVersionLong,
            String scryfallId,
            LanguageIndexDeltaChangeType changeType,
            String localizedName,
            LocalDateTime createdAt
    ) {
        IndexLanguageDeltaEntry entry = new IndexLanguageDeltaEntry();
        entry.setLanguageCode(languageCode);
        entry.setTargetVersion(targetVersion);
        entry.setTargetVersionLong(targetVersionLong);
        entry.setScryfallId(scryfallId);
        entry.setChangeType(changeType);
        entry.setLocalizedName(localizedName);
        entry.setCreatedAt(createdAt);
        return entry;
    }

    private void saveRowStatesInBatches(List<IndexLanguageRowState> rowsToSave) {
        if (rowsToSave.isEmpty()) {
            return;
        }
        for (int i = 0; i < rowsToSave.size(); i += PAGE_SIZE) {
            int end = Math.min(i + PAGE_SIZE, rowsToSave.size());
            indexLanguageRowStateRepository.saveAll(rowsToSave.subList(i, end));
        }
    }

    private void saveDeltaEntriesInBatches(List<IndexLanguageDeltaEntry> deltaEntries) {
        if (deltaEntries.isEmpty()) {
            return;
        }
        for (int i = 0; i < deltaEntries.size(); i += PAGE_SIZE) {
            int end = Math.min(i + PAGE_SIZE, deltaEntries.size());
            indexLanguageDeltaEntryRepository.saveAll(deltaEntries.subList(i, end));
        }
    }

    public record DiffResult(long upsertsCount, long deletesCount) {}
}
