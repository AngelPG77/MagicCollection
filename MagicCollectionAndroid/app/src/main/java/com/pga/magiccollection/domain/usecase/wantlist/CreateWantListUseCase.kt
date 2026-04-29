package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.local.entities.WantListEntity
import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class CreateWantListUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(name: String, userId: Long): WantListEntity {
        return wantListRepository.createWantList(name, userId)
    }
}
