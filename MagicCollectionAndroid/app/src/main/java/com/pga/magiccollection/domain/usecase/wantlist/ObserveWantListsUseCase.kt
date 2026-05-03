package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.local.dao.WantListWithCount
import com.pga.magiccollection.data.repository.WantListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWantListsUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    operator fun invoke(userId: Long): Flow<List<WantListWithCount>> {
        return wantListRepository.observeWantLists(userId)
    }
}
