package com.pga.magiccollection.domain.usecase.wantlist

import com.pga.magiccollection.data.repository.WantListRepository
import javax.inject.Inject

class UpdateCardInWantListUseCase @Inject constructor(
    private val repository: WantListRepository
) {
    suspend operator fun invoke(
        localId: Long,
        quantity: Int,
        foil: Boolean,
        language: String,
        condition: String
    ) {
        repository.updateCardInWantList(localId, quantity, foil, language, condition)
    }
}
