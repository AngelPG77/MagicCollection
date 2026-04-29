package com.pga.magiccollection.domain.usecase.card

import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.domain.model.search.CardIndexQuery
import com.pga.magiccollection.domain.model.search.IndexedCard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveIndexedCardsUseCase @Inject constructor(
    private val cardSearchIndexRepository: CardSearchIndexRepository
) {
    operator fun invoke(query: CardIndexQuery): Flow<List<IndexedCard>> {
        return cardSearchIndexRepository.observeCards(query)
    }
}
