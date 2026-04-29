package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.local.entities.WantListCardEntity
import com.pga.magiccollection.data.repository.WantListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveWantListCardsUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    operator fun invoke(wantListLocalId: Long): Flow<List<WantListCardEntity>> {
        return wantListRepository.observeWantListCards(wantListLocalId)
    }
}
