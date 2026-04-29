package com.pga.magiccollection.ui.screen

import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.IndexedCard
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.data.local.entities.MtgSetEntity

data class SearchUiState(
    val query: String = "",
    val confirmedQuery: String = "",
    val selectedColors: Set<String> = emptySet(),
    val useColorIdentity: Boolean = false,
    val colorLogic: ColorMatchMode = ColorMatchMode.EXACTLY,
    val type: String = "",
    val confirmedType: String = "",
    val selectedRarities: Set<String> = emptySet(),
    val setQuery: String = "",
    val selectedSetCode: String? = null,
    val selectedSetName: String? = null,
    val availableSets: List<MtgSetEntity> = emptyList(),
    val filteredSets: List<MtgSetEntity> = emptyList(),
    val availableTypes: List<String> = emptyList(),
    val filteredTypes: List<String> = emptyList(),
    val sortBy: SearchSortBy = SearchSortBy.NAME,
    val sortAscending: Boolean = true,
    val activeLanguage: String = "en",
    val availableLanguages: List<String> = listOf("en"),
    val searchResults: List<IndexedCard> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasIndexData: Boolean = false,
    val isSearchPerformed: Boolean = false
)
