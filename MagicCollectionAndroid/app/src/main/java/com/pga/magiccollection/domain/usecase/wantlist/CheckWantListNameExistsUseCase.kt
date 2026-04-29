package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class CheckWantListNameExistsUseCase @Inject constructor(
    private val wantListRepository: WantListRepository
) {
    suspend operator fun invoke(name: String, userId: Long): Boolean {
        return wantListRepository.existsByName(name, userId)
    }
}
