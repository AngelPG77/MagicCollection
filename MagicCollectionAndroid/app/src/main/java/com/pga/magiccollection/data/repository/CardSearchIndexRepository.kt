package com.pga.magiccollection.data.repository

import android.util.JsonReader
import android.util.JsonToken
import androidx.room.withTransaction
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import com.pga.magiccollection.data.local.MagicDatabase
import com.pga.magiccollection.data.local.dao.CardSearchIndexDao
import com.pga.magiccollection.data.local.dao.LanguageIndexStateDao
import com.pga.magiccollection.data.local.dao.MasterCardSummaryRow
import com.pga.magiccollection.data.local.dao.MtgSetDao
import com.pga.magiccollection.data.local.entities.MasterCardEntity
import com.pga.magiccollection.data.local.entities.CardSearchFtsEntity
import com.pga.magiccollection.data.local.entities.LanguageIndexStateEntity
import com.pga.magiccollection.data.local.entities.MtgSetEntity
import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.dto.CardMetadataIndexDto
import com.pga.magiccollection.data.remote.dto.LanguageDeltaItemDto
import com.pga.magiccollection.data.remote.dto.LanguageIndexManifestDto
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.remote.dto.IndexVersionDto
import com.pga.magiccollection.data.remote.dto.LanguageSyncStatusDto
import com.pga.magiccollection.domain.model.card.ColorMask
import com.pga.magiccollection.domain.model.card.RarityRank
import com.pga.magiccollection.domain.model.search.CardIndexQuery
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.IndexedCard
import com.pga.magiccollection.domain.model.search.SearchSortBy
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CardSearchIndexRepository @Inject constructor(
    private val cardsApi: CardsApi,
    private val cardSearchIndexDao: CardSearchIndexDao,
    private val languageIndexStateDao: LanguageIndexStateDao,
    private val mtgSetDao: MtgSetDao,
    private val database: MagicDatabase
) {
    private val bootstrapMutex = Mutex()
    private val activeBootstraps = ConcurrentHashMap<String, Boolean>()
    private companion object {
        const val DB_CHUNK_SIZE = 500
        const val NETWORK_PAGE_SIZE = 1000
        const val PROGRESS_STEP = 0.02f
        const val STATE_READY = "READY"
        const val STATE_DOWNLOADING = "DOWNLOADING"
        const val STATE_FAILED = "FAILED"
        val FTS_TERM_REGEX = Regex("[\\p{L}\\p{N}]+")
    }

    fun observeCards(query: CardIndexQuery): Flow<List<IndexedCard>> {
        return cardSearchIndexDao.observeCards(buildSearchQuery(query, paged = false))
            .map { rows -> rows.map { it.toIndexedCard() } }
    }

    fun observeCardsPaged(query: CardIndexQuery): Flow<PagingData<IndexedCard>> {
        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false,
                initialLoadSize = 100
            ),
            pagingSourceFactory = { cardSearchIndexDao.observeCardsPaged(buildSearchQuery(query, paged = true)) }
        ).flow.map { pagingData ->
            pagingData.map { row -> row.toIndexedCard() }
        }
    }

    private fun com.pga.magiccollection.data.local.dao.CardSearchRow.toIndexedCard(): IndexedCard {
        return IndexedCard(
            scryfallId = scryfallId,
            name = name,
            imageUrl = imageUrl,
            manaCost = manaCost,
            rarity = rarityRankToRaw(rarityRank)
        )
    }

    private fun rarityRankToRaw(rank: Int): String? = when (rank) {
        1 -> "common"
        2 -> "uncommon"
        3 -> "rare"
        4 -> "mythic"
        else -> null
    }

    fun getAllSets(): Flow<List<MtgSetEntity>> = mtgSetDao.getAllSets()

    fun searchSets(query: String): Flow<List<MtgSetEntity>> = mtgSetDao.searchSets(query)

    suspend fun getMasterCardSummariesByIds(scryfallIds: List<String>): Map<String, MasterCardSummaryRow> {
        if (scryfallIds.isEmpty()) {
            return emptyMap()
        }
        return cardSearchIndexDao.getMasterCardSummariesByIds(scryfallIds.distinct())
            .associateBy { it.scryfallId }
    }

    fun hasIndexData(): Flow<Boolean> {
        return cardSearchIndexDao.observeMetadataCount().map { it > 0 }
    }

    suspend fun hasIndexDataSync(): Boolean {
        return cardSearchIndexDao.countMetadata() > 0
    }

    suspend fun hasNamesForLanguage(languageCode: String): Boolean {
        val lang = normalizeLanguage(languageCode)
        return cardSearchIndexDao.countNamesByLanguage(lang) > 0
    }

    suspend fun getIndexVersion(): IndexVersionDto = cardsApi.getIndexVersion()

    suspend fun forceScanScryfall() {
        cardsApi.syncFullCatalog()
    }

    /**
     * Asks the backend whether (a) it is in sync with Scryfall and (b) the per-language
     * manifest, then compares each manifest with the local LanguageIndexState to determine
     * which languages have actually drifted on this device.
     *
     * If [languages] is empty, the backend returns the status for all supported languages.
     */
    suspend fun computeSyncDrift(languages: Collection<String> = emptyList()): SyncDriftReport {
        val normalized = languages.map { normalizeLanguage(it) }
            .filter { it.isNotBlank() }
            .distinct()
        val csv = if (normalized.isEmpty()) null else normalized.joinToString(",")
        val status = cardsApi.getSyncStatus(csv)

        val drifted = mutableListOf<String>()
        for (lang in status.languages) {
            val local = languageIndexStateDao.getState(lang.languageCode)
            val matches = local != null
                && local.installedVersion == lang.version
                && !local.checksum.isNullOrBlank()
                && !lang.checksum.isNullOrBlank()
                && local.checksum.equals(lang.checksum, ignoreCase = true)
                && local.status == STATE_READY
            if (!matches) {
                drifted.add(lang.languageCode)
            }
        }

        return SyncDriftReport(
            backendInSyncWithScryfall = status.scryfallInSync,
            catalogStateMissing = status.catalogStateMissing,
            driftedLanguages = drifted.toList(),
            languageStatuses = status.languages
        )
    }

    suspend fun syncSets() {
        try {
            val remoteSets = cardsApi.getSets()
            val entities = remoteSets.map { dto ->
                MtgSetEntity(
                    code = dto.code,
                    name = dto.name,
                    releaseDate = dto.releaseDate
                )
            }
            mtgSetDao.insertSets(entities)
        } catch (e: Exception) {
            // Silencioso
        }
    }

    suspend fun bootstrapIndex(language: String, onProgress: suspend (Float) -> Unit = {}): Int = withContext(Dispatchers.IO) {
        val normalizedLanguage = normalizeLanguage(language)
        
        // Evitar ejecuciones duplicadas para el mismo idioma
        if (activeBootstraps.putIfAbsent(normalizedLanguage, true) == true) {
            android.util.Log.d("CardSearchIndexRepo", "Bootstrap already in progress for $normalizedLanguage, skipping.")
            return@withContext 0
        }

        try {
            bootstrapMutex.withLock {
                syncSets()
                var lastReportedProgress = -1f
                emitProgressIfNeeded(0.05f, onProgress, { lastReportedProgress }, { lastReportedProgress = it }, force = true)

                var count = 0
                var offset = 0
                var hasMore = true
                var totalCards = 0L
                val masterBuffer = mutableListOf<MasterCardEntity>()
                val nameBuffer = mutableListOf<CardSearchFtsEntity>()

                while (hasMore) {
                    val page = cardsApi.getIndexPageForLanguage(
                        lang = normalizedLanguage,
                        offset = offset,
                        limit = NETWORK_PAGE_SIZE
                    )
                    if (totalCards <= 0) {
                        totalCards = page.totalCards
                    }

                    if (page.items.isEmpty()) {
                        break
                    }

                    page.items.forEach { dto ->
                        masterBuffer.add(toMasterCardEntity(dto))
                        nameBuffer.add(CardSearchFtsEntity(cardId = dto.scryfallId, name = dto.defaultName, oracleText = dto.oracleText, language = "en"))
                        if (normalizedLanguage != "en") {
                            nameBuffer.add(
                                CardSearchFtsEntity(
                                    cardId = dto.scryfallId,
                                    name = dto.localizedName ?: dto.defaultName,
                                    oracleText = dto.oracleText,
                                    language = normalizedLanguage
                                )
                            )
                        }
                        count++

                        if (masterBuffer.size >= DB_CHUNK_SIZE) {
                            flushBuffers(masterBuffer, nameBuffer, normalizedLanguage)
                        }
                    }

                    emitProgressIfNeeded(
                        calculateProgress(count, totalCards),
                        onProgress,
                        { lastReportedProgress },
                        { lastReportedProgress = it }
                    )
                    offset = page.offset + page.items.size
                    hasMore = page.hasMore
                }

                if (masterBuffer.isNotEmpty()) {
                    flushBuffers(masterBuffer, nameBuffer, normalizedLanguage)
                }

                emitProgressIfNeeded(1.0f, onProgress, { lastReportedProgress }, { lastReportedProgress = it }, force = true)
                count
            }
        } finally {
            activeBootstraps.remove(normalizedLanguage)
        }
    }

    private suspend fun flushBuffers(
        masterCards: MutableList<MasterCardEntity>,
        names: MutableList<CardSearchFtsEntity>,
        lang: String
    ) {
        database.withTransaction {
            cardSearchIndexDao.upsertMasterCards(masterCards)
            val ids = masterCards.map { it.scryfallId }
            cardSearchIndexDao.deleteNamesByLanguage(ids, "en")
            if (lang != "en") {
                cardSearchIndexDao.deleteNamesByLanguage(ids, lang)
            }
            cardSearchIndexDao.insertNames(names)
        }
        masterCards.clear()
        names.clear()
    }

    suspend fun downloadLanguage(languageCode: String, onProgress: suspend (Float) -> Unit): Int = withContext(Dispatchers.IO) {
        val lang = normalizeLanguage(languageCode)
        var lastReportedProgress = -1f
        emitProgressIfNeeded(0.05f, onProgress, { lastReportedProgress }, { lastReportedProgress = it }, force = true)
        val manifest = cardsApi.getLanguageManifest(lang)
        val currentState = languageIndexStateDao.getState(lang)

        if (
            currentState != null &&
            currentState.installedVersion == manifest.version &&
            !currentState.checksum.isNullOrBlank() &&
            currentState.checksum == manifest.checksum &&
            currentState.status == STATE_READY
        ) {
            emitProgressIfNeeded(1.0f, onProgress, { lastReportedProgress }, { lastReportedProgress = it }, force = true)
            return@withContext 0
        }

        updateLanguageState(lang, currentState, STATE_DOWNLOADING)
        val downloadedCount = try {
            if (currentState?.installedVersion != null && manifest.deltaAvailable) {
                val deltaAttempt = runCatching { cardsApi.getLanguageDelta(lang, currentState.installedVersion) }
                if (deltaAttempt.isSuccess) {
                    val delta = deltaAttempt.getOrThrow()
                    if (delta.upserts.isNotEmpty() || delta.deletes.isNotEmpty()) {
                        applyLanguageDelta(lang, delta.upserts, delta.deletes)
                    }
                    val finalCount = cardSearchIndexDao.countNamesByLanguage(lang)
                    updateLanguageState(
                        lang = lang,
                        previous = currentState,
                        status = STATE_READY,
                        installedVersion = delta.targetVersion,
                        checksum = delta.checksum,
                        rowCount = finalCount,
                        lastError = null
                    )
                    finalCount.toInt()
                } else {
                    val snapshotResult = applyLanguageSnapshot(lang, manifest) { progress ->
                        emitProgressIfNeeded(progress, onProgress, { lastReportedProgress }, { lastReportedProgress = it })
                    }
                    updateLanguageState(
                        lang = lang,
                        previous = currentState,
                        status = STATE_READY,
                        installedVersion = manifest.version,
                        checksum = snapshotResult.checksum,
                        rowCount = snapshotResult.rows.toLong(),
                        lastError = null
                    )
                    snapshotResult.rows
                }
            } else {
                val snapshotResult = applyLanguageSnapshot(lang, manifest) { progress ->
                    emitProgressIfNeeded(progress, onProgress, { lastReportedProgress }, { lastReportedProgress = it })
                }
                updateLanguageState(
                    lang = lang,
                    previous = currentState,
                    status = STATE_READY,
                    installedVersion = manifest.version,
                    checksum = snapshotResult.checksum,
                    rowCount = snapshotResult.rows.toLong(),
                    lastError = null
                    )
                snapshotResult.rows
            }
        } catch (error: Exception) {
            updateLanguageState(
                lang = lang,
                previous = currentState,
                status = STATE_FAILED,
                installedVersion = currentState?.installedVersion,
                checksum = currentState?.checksum,
                rowCount = currentState?.rowCount ?: 0L,
                lastError = error.message
            )
            throw error
        }

        emitProgressIfNeeded(1.0f, onProgress, { lastReportedProgress }, { lastReportedProgress = it }, force = true)
        downloadedCount
    }

    suspend fun ingestRemoteCards(cards: List<ScryfallCardDto>, language: String) = withContext(Dispatchers.IO) {
        if (cards.isEmpty()) return@withContext
        val lang = normalizeLanguage(language)

        val masterEntities = cards.filter {
            val name = it.name
            it.scryfallId != null && !name.startsWith("A-")
        }.map { remote ->
            val finalCmc = (remote.cmc ?: remote.convertedManaCost)?.toFloat()
            MasterCardEntity(
                scryfallId = remote.scryfallId!!,
                name = remote.name,
                printedName = remote.name,
                setCode = remote.setCode,
                typeLine = remote.typeLine,
                manaCost = remote.manaCost,
                cmc = finalCmc,
                convertedManaCost = finalCmc?.toInt(),
                rarityRank = remote.rarityRank ?: RarityRank.fromCode(remote.rarity),
                colorMask = remote.colorMask ?: ColorMask.fromSymbolsOrMana(remote.colors, remote.manaCost),
                identityMask = remote.identityMask ?: ColorMask.fromSymbols(remote.colorIdentity),
                imageUrl = remote.imageUris?.small ?: remote.imageUris?.normal
            )
        }

        val nameEntities = cards.filter { it.scryfallId != null && !it.name.startsWith("A-") }.flatMap { remote ->
            val list = mutableListOf<CardSearchFtsEntity>()
            list.add(CardSearchFtsEntity(cardId = remote.scryfallId!!, name = remote.name, oracleText = remote.oracleText, language = "en"))
            if (lang != "en" && remote.printedName != null) {
                list.add(CardSearchFtsEntity(cardId = remote.scryfallId!!, name = remote.printedName, oracleText = remote.oracleText, language = lang))
            }
            list
        }

        database.withTransaction {
            cardSearchIndexDao.upsertMasterCards(masterEntities)
            val ids = masterEntities.map { it.scryfallId }
            cardSearchIndexDao.deleteNamesByLanguage(ids, "en")
            if (lang != "en") cardSearchIndexDao.deleteNamesByLanguage(ids, lang)
            cardSearchIndexDao.insertNames(nameEntities)
        }
    }

    private suspend fun flushLanguageNames(names: MutableList<CardSearchFtsEntity>) {
        cardSearchIndexDao.insertNames(names)
        names.clear()
    }

    private fun toMasterCardEntity(dto: CardMetadataIndexDto): MasterCardEntity {
        return MasterCardEntity(
            scryfallId = dto.scryfallId,
            name = dto.defaultName,
            printedName = dto.defaultName,
            setCode = dto.setCode,
            typeLine = dto.typeLine,
            manaCost = dto.manaCost,
            cmc = dto.cmc,
            convertedManaCost = dto.cmc?.toInt(),
            rarityRank = dto.rarityRank,
            colorMask = dto.colorMask,
            identityMask = dto.identityMask,
            imageUrl = dto.imageUrl
        )
    }

    private fun calculateProgress(processed: Int, total: Long): Float {
        if (total <= 0L) {
            return 0.85f
        }
        return 0.1f + ((processed.toFloat() / total.toFloat()) * 0.8f).coerceAtMost(0.85f)
    }

    private suspend fun emitProgressIfNeeded(
        progress: Float,
        onProgress: suspend (Float) -> Unit,
        getLast: () -> Float,
        setLast: (Float) -> Unit,
        force: Boolean = false
    ) {
        val normalized = progress.coerceIn(0f, 1f)
        val last = getLast()
        val shouldEmit = force ||
                last < 0f ||
                normalized >= 1f ||
                (normalized - last) >= PROGRESS_STEP

        if (shouldEmit) {
            setLast(normalized)
            onProgress(normalized)
        }
    }

    private fun buildSearchQuery(query: CardIndexQuery, paged: Boolean = false): SupportSQLiteQuery {
        val lang = normalizeLanguage(query.language)
        
        // Fallback robusto: idioma activo -> inglés -> nombre base
        val displayNameExpr = "COALESCE(fts.name, m.printedName, m.name)"

        val sql = StringBuilder()
        val args = mutableListOf<Any>()

        sql.append(
            """
            SELECT
                m.scryfallId AS scryfallId,
                $displayNameExpr AS name,
                m.imageUrl AS imageUrl,
                m.manaCost AS manaCost,
                m.rarityRank AS rarityRank
            FROM master_cards m
            JOIN card_search_fts fts ON fts.card_id = m.scryfallId
            WHERE m.isDigital = 0 AND fts.language IN (?, 'en')
            """.trimIndent()
        )
        args.add(lang)

        if (query.searchText.isNotBlank()) {
            val ftsQuery = buildFtsPrefixQuery(query.searchText)
            if (ftsQuery == null) {
                sql.append(" AND 1 = 0")
            } else {
                sql.append(" AND card_search_fts MATCH ?")
                args.add(ftsQuery)
            }
        }
        
        if (query.typeText.isNotBlank()) {
            sql.append(" AND m.typeLine LIKE ? COLLATE NOCASE")
            args.add("%${query.typeText.trim()}%")
        }

        if (query.setCode != null) {
            sql.append(" AND m.setCode = ?")
            args.add(query.setCode)
        }

        if (query.rarityRanks.isNotEmpty()) {
            val sortedRanks = query.rarityRanks.sorted()
            sql.append(" AND m.rarityRank IN (${sortedRanks.joinToString(",") { "?" }})")
            args.addAll(sortedRanks)
        }

        if (query.colorMask != 0) {
            val targetColumn = if (query.useColorIdentity) "m.identityMask" else "m.colorMask"
            when (query.colorMode) {
                ColorMatchMode.EXACTLY -> {
                    sql.append(" AND $targetColumn = ?")
                    args.add(query.colorMask)
                }
                ColorMatchMode.AT_MOST -> {
                    sql.append(" AND ($targetColumn & ~?) = 0")
                    args.add(query.colorMask)
                }
                ColorMatchMode.INCLUDING -> {
                    sql.append(" AND ($targetColumn & ?) = ?")
                    args.add(query.colorMask)
                    args.add(query.colorMask)
                }
            }
        }

        sql.append(" GROUP BY m.name")

        val sortColumn = when (query.sortBy) {
            SearchSortBy.NAME -> "LOWER($displayNameExpr)"
            SearchSortBy.RARITY -> "m.rarityRank"
            SearchSortBy.CMC -> "COALESCE(m.cmc, 0)"
        }
        val direction = if (query.ascending) "ASC" else "DESC"
        sql.append(" ORDER BY $sortColumn $direction, LOWER($displayNameExpr) ASC")
        
        if (!paged) {
            sql.append(" LIMIT ?")
            args.add(query.limit.coerceIn(20, 500))
        }

        return SimpleSQLiteQuery(sql.toString(), args.toTypedArray())
    }

    private fun normalizeLanguage(language: String): String {
        return language.trim().lowercase().ifBlank { "en" }
    }

    private fun buildFtsPrefixQuery(rawQuery: String): String? {
        val terms = FTS_TERM_REGEX
            .findAll(rawQuery.lowercase())
            .map { it.value.trim() }
            .filter { it.isNotBlank() }
            .toList()
        if (terms.isEmpty()) {
            return null
        }
        // En FTS4, las expresiones parentizadas con OR por columna pueden devolver 0 resultados.
        // Para autocompletado estable usamos prefijo por nombre.
        return terms.joinToString(" ") { "name:$it*" }
    }

    private suspend fun applyLanguageSnapshot(
        lang: String,
        manifest: LanguageIndexManifestDto,
        emitProgress: suspend (Float) -> Unit
    ): SnapshotApplyResult {
        var count = 0
        val expectedRows = manifest.totalRows
        val nameBuffer = mutableListOf<CardSearchFtsEntity>()
        val digest = MessageDigest.getInstance("SHA-256")

        cardsApi.downloadLanguageNamesSnapshot(lang).use { responseBody ->
            JsonReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8)).use { reader ->
                database.withTransaction {
                    cardSearchIndexDao.deleteAllNamesByLanguage(lang)
                    reader.beginArray()
                    while (reader.hasNext()) {
                        var scryfallId: String? = null
                        var localizedName: String? = null
                        var oracleText: String? = null

                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.nextName()) {
                                "scryfallId" -> {
                                    if (reader.peek() == JsonToken.NULL) reader.nextNull() else scryfallId = reader.nextString()
                                }
                                "localizedName" -> {
                                    if (reader.peek() == JsonToken.NULL) reader.nextNull() else localizedName = reader.nextString()
                                }
                                "oracleText" -> {
                                    if (reader.peek() == JsonToken.NULL) reader.nextNull() else oracleText = reader.nextString()
                                }
                                else -> reader.skipValue()
                            }
                        }
                        reader.endObject()

                        if (!scryfallId.isNullOrBlank() && !localizedName.isNullOrBlank()) {
                            nameBuffer.add(CardSearchFtsEntity(cardId = scryfallId, name = localizedName, oracleText = oracleText, language = lang))
                            digest.update("$scryfallId|$localizedName\n".toByteArray(Charsets.UTF_8))
                            count++
                        }

                        if (nameBuffer.size >= DB_CHUNK_SIZE) {
                            flushLanguageNames(nameBuffer)
                        }
                        emitProgress(calculateProgress(count, expectedRows))
                    }
                    reader.endArray()
                    if (nameBuffer.isNotEmpty()) {
                        flushLanguageNames(nameBuffer)
                    }
                }
            }
        }

        val checksum = digest.digest().joinToString("") { "%02x".format(it) }
        if (!checksum.equals(manifest.checksum, ignoreCase = true)) {
            throw LanguageSnapshotChecksumException("Checksum mismatch for $lang")
        }
        return SnapshotApplyResult(rows = count, checksum = checksum)
    }

    private suspend fun applyLanguageDelta(
        lang: String,
        upserts: List<LanguageDeltaItemDto>,
        deletes: List<String>
    ) {
        database.withTransaction {
            if (deletes.isNotEmpty()) {
                cardSearchIndexDao.deleteNamesByLanguage(deletes, lang)
            }
            if (upserts.isNotEmpty()) {
                val ids = upserts.map { it.scryfallId }
                cardSearchIndexDao.deleteNamesByLanguage(ids, lang)
                val entities = upserts.map { CardSearchFtsEntity(cardId = it.scryfallId, name = it.localizedName, oracleText = it.oracleText, language = lang) }
                cardSearchIndexDao.insertNames(entities)
            }
        }
    }

    private suspend fun updateLanguageState(
        lang: String,
        previous: LanguageIndexStateEntity?,
        status: String,
        installedVersion: String? = previous?.installedVersion,
        checksum: String? = previous?.checksum,
        rowCount: Long = previous?.rowCount ?: 0L,
        lastError: String? = previous?.lastError
    ) {
        languageIndexStateDao.upsert(
            LanguageIndexStateEntity(
                languageCode = lang,
                installedVersion = installedVersion,
                checksum = checksum,
                rowCount = rowCount,
                lastSyncAt = System.currentTimeMillis(),
                status = status,
                lastError = lastError
            )
        )
    }

    private data class SnapshotApplyResult(
        val rows: Int,
        val checksum: String
    )

}

class LanguageSnapshotChecksumException(message: String) : IllegalStateException(message)

data class SyncDriftReport(
    val backendInSyncWithScryfall: Boolean,
    val catalogStateMissing: Boolean,
    val driftedLanguages: List<String>,
    val languageStatuses: List<LanguageSyncStatusDto>
) {
    val isUpToDate: Boolean
        get() = backendInSyncWithScryfall && driftedLanguages.isEmpty() && !catalogStateMissing
}
