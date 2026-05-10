package com.pga.magiccollection.domain.usecase.settings

import com.pga.magiccollection.data.local.dao.CollectionCardDao
import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.WantListCardDao
import com.pga.magiccollection.data.local.dao.WantListDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Emits `true` when every locally-tracked entity (collections, wantlists and the cards
 * inside them) has `synced = 1`, i.e. nothing pending to push to the backend.
 *
 * Uses `collection_cards` as the single source of truth for owned cards — the legacy
 * `cards_owned` table was removed in DB v17.
 */
class ObserveSyncStatusUseCase @Inject constructor(
    private val collectionDao: CollectionDao,
    private val wantListDao: WantListDao,
    private val collectionCardDao: CollectionCardDao,
    private val wantListCardDao: WantListCardDao
) {
    operator fun invoke(userId: Long): Flow<Boolean> {
        return combine(
            collectionDao.observeUnsyncedCollectionsCount(userId),
            wantListDao.observeUnsyncedWantListsCount(userId),
            collectionCardDao.observeUnsyncedCardsCount(userId),
            wantListCardDao.observeUnsyncedCardsCount(userId)
        ) { counts ->
            counts.all { it == 0 }
        }
    }
}
