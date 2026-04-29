package com.pga.magiccollection.domain.model.search

enum class ColorMatchMode(val apiValue: String) {
    EXACTLY("exactly"),
    AT_MOST("at_most"),
    INCLUDING("including")
}

enum class SearchSortBy {
    NAME,
    RARITY,
    CMC
}

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

data class IndexedCard(
    val scryfallId: String,
    val name: String,
    val imageUrl: String?
)
