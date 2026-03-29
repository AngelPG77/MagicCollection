package com.pga.magiccollection.domain.usecase.home

import com.pga.magiccollection.data.local.dao.RecentCardDao
import com.pga.magiccollection.data.local.entities.RecentCardEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentCardsUseCase @Inject constructor(
    private val recentCardDao: RecentCardDao
) {
    operator fun invoke(): Flow<List<RecentCardEntity>> {
        return recentCardDao.observeRecentCards()
    }
}
