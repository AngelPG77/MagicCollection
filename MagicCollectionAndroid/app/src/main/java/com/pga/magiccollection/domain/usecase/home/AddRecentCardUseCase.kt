package com.pga.magiccollection.domain.usecase.home

import com.pga.magiccollection.data.local.dao.RecentCardDao
import com.pga.magiccollection.data.local.entities.RecentCardEntity
import javax.inject.Inject

class AddRecentCardUseCase @Inject constructor(
    private val recentCardDao: RecentCardDao
) {
    suspend operator fun invoke(scryfallId: String, name: String, imageUrl: String?) {
        recentCardDao.insertAndTrim(
            RecentCardEntity(
                scryfallId = scryfallId,
                name = name,
                imageUrl = imageUrl,
                visitedAt = System.currentTimeMillis()
            )
        )
    }
}
