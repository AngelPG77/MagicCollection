package com.pga.magiccollection.domain.model.search

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
enum class ColorMatchMode(val apiValue: String) {
    EXACTLY("exactly"),
    AT_MOST("at_most"),
    INCLUDING("including")
}

@Stable
enum class SearchSortBy {
    NAME,
    RARITY,
    CMC
}

@Immutable
data class CardIndexQuery(
    val searchText: String = "",
    val language: String = "en",
    val colorMask: Int = 0,
    val useColorIdentity: Boolean = false,
    val colorMode: ColorMatchMode = ColorMatchMode.EXACTLY,
    val typeText: String = "",
    val rarityRanks: Set<Int> = emptySet(),
    val setCode: String? = null,
    val sortBy: SearchSortBy = SearchSortBy.NAME,
    val ascending: Boolean = true,
    val limit: Int = 200
)

@Immutable
data class IndexedCard(
    val scryfallId: String,
    val name: String,
    val imageUrl: String?
)
