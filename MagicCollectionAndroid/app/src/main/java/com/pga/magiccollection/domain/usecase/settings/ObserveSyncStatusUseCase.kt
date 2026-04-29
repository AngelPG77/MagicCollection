package com.pga.magiccollection.domain.usecase.settings

import com.pga.magiccollection.data.local.dao.CollectionDao
import com.pga.magiccollection.data.local.dao.WantListDao
import com.pga.magiccollection.data.local.dao.CardOwnedDao
import com.pga.magiccollection.data.local.dao.WantListCardDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ObserveSyncStatusUseCase @Inject constructor(
    private val collectionDao: CollectionDao,
    private val wantListDao: WantListDao,
    private val cardOwnedDao: CardOwnedDao,
    private val wantListCardDao: WantListCardDao
) {
    operator fun invoke(userId: Long): Flow<Boolean> {
        return combine(
            collectionDao.observeUnsyncedCollectionsCount(userId),
            wantListDao.observeUnsyncedWantListsCount(userId),
            cardOwnedDao.observeUnsyncedCardsCount(userId),
            wantListCardDao.observeUnsyncedCardsCount(userId)
        ) { counts ->
            counts.all { it == 0 }
        }
    }
}
