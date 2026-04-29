package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class DeleteWantListUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(localId: Long) {
        wantListRepository.deleteWantList(localId)
    }
}
