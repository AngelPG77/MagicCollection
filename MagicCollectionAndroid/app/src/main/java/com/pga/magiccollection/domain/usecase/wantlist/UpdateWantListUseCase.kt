package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class UpdateWantListUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(localId: Long, newName: String) {
        wantListRepository.updateWantList(localId, newName)
    }
}
