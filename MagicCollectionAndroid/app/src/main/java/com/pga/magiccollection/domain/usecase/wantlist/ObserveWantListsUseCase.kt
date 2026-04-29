package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.local.entities.WantListEntity
import com.pga.magiccollection.data.repository.WantListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWantListsUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    operator fun invoke(userId: Long): Flow<List<WantListEntity>> {
        return wantListRepository.observeWantLists(userId)
    }
}
