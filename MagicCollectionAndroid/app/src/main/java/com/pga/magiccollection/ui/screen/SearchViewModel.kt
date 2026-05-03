package com.pga.magiccollection.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.domain.model.card.ColorMask
import com.pga.magiccollection.domain.model.card.RarityRank
import com.pga.magiccollection.domain.model.search.CardIndexQuery
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.domain.usecase.card.BootstrapCardIndexUseCase
import com.pga.magiccollection.domain.usecase.card.HasCardIndexDataUseCase
import com.pga.magiccollection.domain.usecase.card.IngestRemoteCardsUseCase
import com.pga.magiccollection.domain.usecase.card.ObserveIndexedCardsUseCase
import com.pga.magiccollection.domain.usecase.card.SearchCardsUseCase
import com.pga.magiccollection.domain.usecase.settings.GetAppPreferencesUseCase
import com.pga.magiccollection.domain.usecase.settings.UpdateAppPreferenceUseCase
import com.pga.magiccollection.data.local.entities.MtgSetEntity
import com.pga.magiccollection.data.local.dao.MtgSetDao
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.util.ErrorParser
import androidx.paging.cachedIn
import androidx.paging.PagingData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val searchCardsUseCase: SearchCardsUseCase,
    private val observeIndexedCardsUseCase: ObserveIndexedCardsUseCase,
    private val bootstrapCardIndexUseCase: BootstrapCardIndexUseCase,
    private val ingestRemoteCardsUseCase: IngestRemoteCardsUseCase,
    private val hasCardIndexDataUseCase: HasCardIndexDataUseCase,
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase,
    private val updateAppPreferenceUseCase: UpdateAppPreferenceUseCase,
    private val cardSearchIndexRepository: CardSearchIndexRepository
) : ViewModel() {
    private companion object {
        const val SUGGESTIONS_LIMIT = 20
        const val SEARCH_RESULTS_LIMIT = 200
        const val DEBOUNCE_MS = 300L
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _navigateToDetailEvent = MutableSharedFlow<String>()
    val navigateToDetailEvent = _navigateToDetailEvent.asSharedFlow()

    private val commonTypes = listOf(
        "Creature", "Instant", "Sorcery", "Artifact", "Enchantment", 
        "Land", "Planeswalker", "Legendary", "Aura", "Equipment"
    )

    init {
        _uiState.update { it.copy(availableTypes = commonTypes, filteredTypes = commonTypes) }
        observeLanguagePreference()
        observeLocalResults()
        observeSets()
        checkIfIndexAlreadyExists()
    }

    private fun observeSets() {
        viewModelScope.launch {
            cardSearchIndexRepository.getAllSets().collect { sets ->
                _uiState.update { it.copy(availableSets = sets, filteredSets = sets) }
            }
        }
    }

    private fun observeLanguagePreference() {
        viewModelScope.launch {
            getAppPreferencesUseCase()
                .map { preferences ->
                    val availableLanguages = normalizeDownloadedLanguages(preferences.downloadedLanguages)
                    val preferredLanguage = normalizeLanguage(preferences.searchLanguage)
                    val effectiveLanguage = preferredLanguage.takeIf { it in availableLanguages } ?: availableLanguages.first()
                    LanguagePreferenceState(
                        preferredLanguage = preferredLanguage,
                        effectiveLanguage = effectiveLanguage,
                        availableLanguages = availableLanguages
                    )
                }
                .distinctUntilChanged()
                .collect { languageState ->
                    _uiState.update {
                        it.copy(
                            activeLanguage = languageState.effectiveLanguage,
                            availableLanguages = languageState.availableLanguages
                        )
                    }
                    
                    // Verificación proactiva de integridad solo si ha cambiado el idioma efectivo
                    checkAndRepairLanguageIntegrity(languageState.effectiveLanguage)

                    if (languageState.preferredLanguage != languageState.effectiveLanguage) {
                        updateAppPreferenceUseCase.setSearchLanguage(languageState.effectiveLanguage)
                    }
                }
        }
    }

    fun onQueryConfirmed(value: String) {
        _uiState.update { it.copy(confirmedQuery = value) }
    }

    fun onTypeConfirmed(value: String) {
        _uiState.update { it.copy(confirmedType = value, type = value) }
    }

    private fun checkAndRepairLanguageIntegrity(language: String) {
        viewModelScope.launch {
            val hasData = cardSearchIndexRepository.hasNamesForLanguage(language)
            if (!hasData) {
                bootstrapIndexForLanguage(language)
            }
        }
    }

    private fun observeLocalResults() {
        viewModelScope.launch {
            val queryFlow = combine(
                _searchQuery.debounce(DEBOUNCE_MS).distinctUntilChanged(),
                _uiState.map { it.selectedColors }.distinctUntilChanged(),
                _uiState.map { it.useColorIdentity }.distinctUntilChanged(),
                _uiState.map { it.colorLogic }.distinctUntilChanged(),
                _uiState.map { it.type }.distinctUntilChanged(),
                _uiState.map { it.selectedRarities }.distinctUntilChanged(),
                _uiState.map { it.activeLanguage }.distinctUntilChanged(),
                _uiState.map { it.isSearchPerformed }.distinctUntilChanged(),
                _uiState.map { it.sortBy }.distinctUntilChanged(),
                _uiState.map { it.sortAscending }.distinctUntilChanged()
            ) { params ->
                val query = params[0] as String
                val isSearchPerformed = params[7] as Boolean
                val hasFilters = (params[1] as Set<*>).isNotEmpty() || (params[4] as String).isNotBlank()
                
                if (query.isBlank() && !isSearchPerformed && !hasFilters) {
                    null
                } else {
                    _uiState.value.toCardIndexQuery().copy(searchText = query)
                }
            }.distinctUntilChanged()

            // Handle standard Flow for suggestions/small lists
            launch {
                queryFlow.flatMapLatest { query ->
                    if (query == null) {
                        flowOf(emptyList())
                    } else {
                        observeIndexedCardsUseCase(query)
                    }
                }.collect { cards ->
                    _uiState.update { it.copy(searchResults = cards) }
                }
            }

            // Handle Paged Flow for full search results
            val pagedFlow = queryFlow.flatMapLatest { query ->
                if (query == null) {
                    flowOf(PagingData.empty())
                } else {
                    observeIndexedCardsUseCase.invokePaged(query)
                }
            }.cachedIn(viewModelScope)

            _uiState.update { it.copy(pagedSearchResults = pagedFlow) }
        }
    }

    private fun checkIfIndexAlreadyExists() {
        viewModelScope.launch {
            val preferences = getAppPreferencesUseCase().first()
            val availableLanguages = normalizeDownloadedLanguages(preferences.downloadedLanguages)
            val preferredLanguage = normalizeLanguage(preferences.searchLanguage)
            val language = preferredLanguage.takeIf { it in availableLanguages } ?: availableLanguages.first()
            
            hasCardIndexDataUseCase().collect { hasData ->
                _uiState.update { it.copy(hasIndexData = hasData, activeLanguage = language) }
                if (!hasData) {
                    bootstrapIndexForLanguage(language)
                }
            }
        }
    }

    private suspend fun bootstrapIndexForLanguage(language: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        try {
            val loadedCount = bootstrapCardIndexUseCase(language)
            _uiState.update { state ->
                state.copy(
                    hasIndexData = loadedCount > 0 || state.hasIndexData
                )
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = ErrorParser.parseError(e)) }
        } finally {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onQueryChanged(value: String) {
        _searchQuery.value = value
        _uiState.update { 
            it.copy(
                query = value, 
                errorMessage = null, 
                isSearchPerformed = false
            )
        }
    }

    fun onClearQuery() {
        _searchQuery.value = ""
        _uiState.update { 
            it.copy(
                query = "",
                confirmedQuery = "",
                isSearchPerformed = false
            )
        }
    }

    fun onLanguageSelected(language: String) {
        viewModelScope.launch {
            updateAppPreferenceUseCase.setSearchLanguage(normalizeLanguage(language))
        }
    }

    fun onColorToggled(color: String) {
        _uiState.update { state ->
            val normalized = color.trim().uppercase()
            val newColors = if (normalized in state.selectedColors) {
                state.selectedColors - normalized
            } else {
                state.selectedColors + normalized
            }
            state.copy(selectedColors = newColors)
        }
    }

    fun onUseColorIdentityChanged(value: Boolean) {
        _uiState.update { it.copy(useColorIdentity = value) }
    }

    fun onColorLogicChanged(value: ColorMatchMode) {
        _uiState.update { it.copy(colorLogic = value) }
    }

    fun onTypeChanged(value: String) {
        _uiState.update { it.copy(type = value) }
    }

    fun onTypeQueryChanged(value: String) {
        _uiState.update { state ->
            val filtered = if (value.isBlank()) {
                state.availableTypes
            } else {
                state.availableTypes.filter { it.contains(value, ignoreCase = true) }
            }
            state.copy(filteredTypes = filtered)
        }
    }

    fun onRarityToggled(rarity: String) {
        _uiState.update { state ->
            val normalized = rarity.trim().lowercase()
            val newRarities = if (normalized in state.selectedRarities) {
                state.selectedRarities - normalized
            } else {
                state.selectedRarities + normalized
            }
            state.copy(selectedRarities = newRarities)
        }
    }

    fun onSortByChanged(sortBy: SearchSortBy) {
        _uiState.update { it.copy(sortBy = sortBy) }
    }

    fun onSortAscendingChanged(ascending: Boolean) {
        _uiState.update { it.copy(sortAscending = ascending) }
    }

    fun onSetQueryChanged(value: String) {
        _uiState.update { state ->
            val filtered = if (value.isBlank()) {
                state.availableSets
            } else {
                state.availableSets.filter { 
                    it.name.contains(value, ignoreCase = true) || it.code.contains(value, ignoreCase = true)
                }
            }
            state.copy(setQuery = value, filteredSets = filtered)
        }
    }

    fun onSetSelected(set: MtgSetEntity) {
        _uiState.update { it.copy(selectedSetCode = set.code, selectedSetName = set.name, setQuery = "") }
    }

    fun onClearSet() {
        _uiState.update { it.copy(selectedSetCode = null, selectedSetName = null, setQuery = "") }
    }

    fun performSearch() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.query.isBlank() && state.selectedColors.isEmpty() && state.type.isBlank() && state.selectedRarities.isEmpty() && state.selectedSetCode == null) {
                return@launch
            }
            _uiState.update { it.copy(isSearchPerformed = true) }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val colorsString = state.selectedColors.sorted().joinToString("").takeIf { it.isNotBlank() }
                val raritiesString = state.selectedRarities.sorted().joinToString(",").takeIf { it.isNotBlank() }

                val results = searchCardsUseCase(
                    query = state.query.ifBlank { null },
                    colors = colorsString,
                    colorIdentity = state.useColorIdentity,
                    colorLogic = state.colorLogic.apiValue,
                    type = state.type.ifBlank { null },
                    rarity = raritiesString,
                    set = state.selectedSetCode,
                    lang = state.activeLanguage
                )

                ingestRemoteCardsUseCase(results, state.activeLanguage)
                
                // Si hay exactamente un resultado y el usuario buscó por nombre, navegamos directamente
                if (results.size == 1 && state.query.isNotBlank()) {
                    results.first().scryfallId?.let { id ->
                        _navigateToDetailEvent.emit(id)
                    }
                }

                _uiState.update { current ->
                    current.copy(hasIndexData = results.isNotEmpty() || current.hasIndexData)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = ErrorParser.parseError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _uiState.update { state ->
            state.copy(
                query = "",
                confirmedQuery = "",
                selectedColors = emptySet(),
                useColorIdentity = false,
                colorLogic = ColorMatchMode.EXACTLY,
                type = "",
                confirmedType = "",
                selectedRarities = emptySet(),
                selectedSetCode = null,
                selectedSetName = null,
                setQuery = "",
                sortBy = SearchSortBy.NAME,
                sortAscending = true,
                errorMessage = null,
                isSearchPerformed = false
            )
        }
    }

    private fun SearchUiState.toCardIndexQuery(): CardIndexQuery {
        val limit = if (!isSearchPerformed && query.isNotBlank()) {
            SUGGESTIONS_LIMIT
        } else {
            SEARCH_RESULTS_LIMIT
        }
        return CardIndexQuery(
            searchText = query,
            language = activeLanguage,
            colorMask = ColorMask.fromSymbols(selectedColors),
            useColorIdentity = useColorIdentity,
            colorMode = colorLogic,
            typeText = type,
            rarityRanks = selectedRarities.mapTo(mutableSetOf()) { rarity ->
                RarityRank.fromCode(rarity)
            },
            setCode = selectedSetCode,
            sortBy = sortBy,
            ascending = sortAscending,
            limit = limit
        )
    }

    private fun SearchUiState.shouldQueryLocalIndex(): Boolean {
        if (query.isNotBlank()) {
            return true
        }
        return isSearchPerformed || selectedColors.isNotEmpty() || type.isNotBlank() || selectedRarities.isNotEmpty()
    }

    private fun normalizeLanguage(language: String): String {
        return language.trim().lowercase().ifBlank { "en" }
    }

    private fun normalizeDownloadedLanguages(downloadedLanguages: Set<String>): List<String> {
        val normalized = downloadedLanguages
            .asSequence()
            .map(::normalizeLanguage)
            .filter { it.isNotBlank() }
            .toMutableSet()
        normalized.add("en")
        return normalized
            .sortedWith(compareBy<String> { if (it == "en") 0 else 1 }.thenBy { it })
    }

    private data class LanguagePreferenceState(
        val preferredLanguage: String,
        val effectiveLanguage: String,
        val availableLanguages: List<String>
    )
}
