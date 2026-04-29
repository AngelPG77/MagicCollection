package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.local.entities.WantListEntity
import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class GetWantListByIdUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(localId: Long): WantListEntity? {
        return wantListRepository.getWantListById(localId)
    }
}
