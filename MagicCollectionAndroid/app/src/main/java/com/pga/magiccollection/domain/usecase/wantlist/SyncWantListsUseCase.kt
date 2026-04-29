package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class SyncWantListsUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(userId: Long): Int {
        return wantListRepository.syncWantLists(userId)
    }
}
