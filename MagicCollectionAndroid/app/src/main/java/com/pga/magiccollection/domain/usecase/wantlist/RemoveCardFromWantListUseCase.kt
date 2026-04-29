package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class RemoveCardFromWantListUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(wantListLocalId: Long, cardLocalId: Long) {
        wantListRepository.removeCardFromWantList(wantListLocalId, cardLocalId)
    }
}
