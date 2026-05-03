package com.pga.magiccollection.ui.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.data.local.entities.MtgSetEntity
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.domain.model.card.ColorMask
import com.pga.magiccollection.domain.model.card.RarityRank
import com.pga.magiccollection.domain.model.search.CardIndexQuery
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.IndexedCard
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.domain.usecase.card.BootstrapCardIndexUseCase
import com.pga.magiccollection.domain.usecase.card.IngestRemoteCardsUseCase
import com.pga.magiccollection.domain.usecase.card.ObserveIndexedCardsUseCase
import com.pga.magiccollection.domain.usecase.card.SearchCardsUseCase
import com.pga.magiccollection.domain.usecase.collection.AddCardToCollectionUseCase
import com.pga.magiccollection.domain.usecase.settings.GetAppPreferencesUseCase
import com.pga.magiccollection.util.ErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CollectionAddCardUiState(
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
    val hasIndexData: Boolean = false,
    val isSearchPerformed: Boolean = false,
    val errorMessage: String? = null,
    
    // Version Selection
    val showVersionModal: Boolean = false,
    val selectedBaseCard: IndexedCard? = null,
    val cardVersions: List<ScryfallCardDto> = emptyList(),
    val isLoadingVersions: Boolean = false,
    
    // Detail Entry
    val showDetailModal: Boolean = false,
    val selectedVersion: ScryfallCardDto? = null,
    val quantity: Int = 1,
    val foil: Boolean = false,
    val condition: String = "NEAR_MINT",
    val detailLanguage: String = "en",
    val isSaving: Boolean = false,
    val message: String? = null
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class CollectionAddCardViewModel @Inject constructor(
    private val observeIndexedCardsUseCase: ObserveIndexedCardsUseCase,
    private val searchCardsUseCase: SearchCardsUseCase,
    private val ingestRemoteCardsUseCase: IngestRemoteCardsUseCase,
    private val addCardToCollectionUseCase: AddCardToCollectionUseCase,
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase,
    private val bootstrapCardIndexUseCase: BootstrapCardIndexUseCase,
    private val cardSearchIndexRepository: CardSearchIndexRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val collectionLocalId: Long = savedStateHandle.get<Long>("localId") ?: 0L

    private val _uiState = MutableStateFlow(CollectionAddCardUiState())
    val uiState: StateFlow<CollectionAddCardUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val commonTypes = listOf(
        "Creature", "Instant", "Sorcery", "Artifact", "Enchantment", 
        "Land", "Planeswalker", "Legendary", "Aura", "Equipment"
    )

    init {
        _uiState.update { it.copy(availableTypes = commonTypes, filteredTypes = commonTypes) }
        observeLanguagePreference()
        observeIndexStatus()
        observeLocalResults()
        observeSets()
    }

    private fun observeIndexStatus() {
        viewModelScope.launch {
            bootstrapCardIndexUseCase.hasIndexData().collect { hasData ->
                _uiState.update { it.copy(hasIndexData = hasData) }
            }
        }
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
                    val availableLangs = (preferences.downloadedLanguages + "en").toList().sorted()
                    val activeLang = preferences.searchLanguage.takeIf { it.isNotBlank() } ?: "en"
                    activeLang to availableLangs
                }
                .distinctUntilChanged()
                .collect { (lang, availableLangs) ->
                    _uiState.update { 
                        it.copy(
                            activeLanguage = lang, 
                            availableLanguages = availableLangs,
                            detailLanguage = lang 
                        ) 
                    }
                    
                    try {
                        val hasLocalData = cardSearchIndexRepository.hasNamesForLanguage(lang)
                        if (!hasLocalData) {
                            bootstrapCardIndexUseCase(lang)
                        }
                    } catch (e: Exception) {
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

    @Suppress("UNCHECKED_CAST")
    private fun observeLocalResults() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300L).distinctUntilChanged(),
                _uiState.map { it.selectedColors }.distinctUntilChanged(),
                _uiState.map { it.useColorIdentity }.distinctUntilChanged(),
                _uiState.map { it.colorLogic }.distinctUntilChanged(),
                _uiState.map { it.type }.distinctUntilChanged(),
                _uiState.map { it.selectedRarities }.distinctUntilChanged(),
                _uiState.map { it.activeLanguage }.distinctUntilChanged(),
                _uiState.map { it.isSearchPerformed }.distinctUntilChanged(),
                _uiState.map { it.sortBy }.distinctUntilChanged(),
                _uiState.map { it.sortAscending }.distinctUntilChanged(),
                _uiState.map { it.hasIndexData }.distinctUntilChanged()
            ) { params ->
                val query = params[0] as String
                val selectedColors = params[1] as Set<String>
                val useColorIdentity = params[2] as Boolean
                val colorLogic = params[3] as ColorMatchMode
                val type = params[4] as String
                val selectedRarities = params[5] as Set<String>
                val language = params[6] as String
                val isSearchPerformed = params[7] as Boolean
                val sortBy = params[8] as SearchSortBy
                val ascending = params[9] as Boolean
                val hasData = params[10] as Boolean
                
                val hasFilters = selectedColors.isNotEmpty() || type.isNotBlank()
                
                if (!hasData || (query.isBlank() && !isSearchPerformed && !hasFilters)) {
                    null
                } else {
                    val limit = if (!isSearchPerformed && query.isNotBlank()) 20 else 200
                    CardIndexQuery(
                        searchText = query,
                        language = language,
                        colorMask = ColorMask.fromSymbols(selectedColors),
                        useColorIdentity = useColorIdentity,
                        colorMode = colorLogic,
                        typeText = type,
                        rarityRanks = selectedRarities.mapTo(mutableSetOf()) { RarityRank.fromCode(it) },
                        setCode = _uiState.value.selectedSetCode,
                        sortBy = sortBy,
                        ascending = ascending,
                        limit = limit
                    )
                }
            }
            .flatMapLatest { indexQuery ->
                if (indexQuery == null) {
                    flowOf(emptyList())
                } else {
                    observeIndexedCardsUseCase(indexQuery)
                }
            }
            .collect { cards ->
                _uiState.update { it.copy(searchResults = cards) }
            }
        }
    }

    fun onQueryChanged(value: String) {
        _searchQuery.value = value
        _uiState.update { it.copy(query = value, isSearchPerformed = false) }
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
        _uiState.update { it.copy(activeLanguage = language) }
        viewModelScope.launch {
            try {
                val hasLocalData = cardSearchIndexRepository.hasNamesForLanguage(language)
                if (!hasLocalData) {
                    bootstrapCardIndexUseCase(language)
                }
            } catch (e: Exception) {}
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
                isSearchPerformed = false
            )
        }
    }

    fun performSearch() {
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSearchPerformed = true, isLoading = true, errorMessage = null) }
            try {
                val results = searchCardsUseCase(
                    query = state.query.ifBlank { null },
                    colors = state.selectedColors.sorted().joinToString("").takeIf { it.isNotBlank() },
                    colorIdentity = state.useColorIdentity,
                    colorLogic = state.colorLogic.apiValue,
                    type = state.type.ifBlank { null },
                    rarity = state.selectedRarities.sorted().joinToString(",").takeIf { it.isNotBlank() },
                    set = state.selectedSetCode,
                    lang = state.activeLanguage
                )
                ingestRemoteCardsUseCase(results, state.activeLanguage)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = ErrorParser.parseError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onCardSelected(card: IndexedCard) {
        _uiState.update { 
            it.copy(
                selectedBaseCard = card, 
                showVersionModal = true, 
                isLoadingVersions = true,
                cardVersions = emptyList()
            ) 
        }
        
        viewModelScope.launch {
            try {
                val versionsQuery = "!\"${card.name}\" include:extras unique:prints"
                val versions = searchCardsUseCase(query = versionsQuery, lang = _uiState.value.activeLanguage)
                _uiState.update { it.copy(cardVersions = versions, isLoadingVersions = false) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        errorMessage = ErrorParser.parseError(e), 
                        isLoadingVersions = false 
                    ) 
                }
            }
        }
    }

    fun onVersionSelected(version: ScryfallCardDto) {
        _uiState.update { 
            it.copy(
                selectedVersion = version,
                showVersionModal = false,
                showDetailModal = true,
                quantity = 1,
                foil = (version.foil == true) && (version.nonfoil == false),
                detailLanguage = _uiState.value.activeLanguage
            ) 
        }
    }

    fun onDismissVersionModal() {
        _uiState.update { it.copy(showVersionModal = false, selectedBaseCard = null, cardVersions = emptyList()) }
    }

    fun onDismissDetailModal() {
        _uiState.update { it.copy(showDetailModal = false, selectedVersion = null) }
    }

    fun onQuantityChanged(value: Int) {
        if (value >= 1) {
            _uiState.update { it.copy(quantity = value) }
        }
    }

    fun onFoilChanged(value: Boolean) {
        _uiState.update { it.copy(foil = value) }
    }

    fun onConditionChanged(value: String) {
        _uiState.update { it.copy(condition = value) }
    }

    fun onLanguageChanged(value: String) {
        _uiState.update { it.copy(detailLanguage = value) }
    }

    fun saveCard() {
        val version = _uiState.value.selectedVersion ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                addCardToCollectionUseCase(
                    collectionLocalId = collectionLocalId,
                    scryfallId = version.scryfallId ?: "",
                    name = version.name,
                    typeLine = version.typeLine,
                    manaCost = version.manaCost,
                    imageUrl = version.imageUris?.normal ?: version.imageUris?.small,
                    quantity = _uiState.value.quantity,
                    foil = _uiState.value.foil,
                    language = _uiState.value.detailLanguage,
                    condition = _uiState.value.condition
                )
                _uiState.update { 
                    it.copy(
                        isSaving = false, 
                        showDetailModal = false, 
                        message = "collection_card_added_success",
                        isSearchPerformed = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, errorMessage = ErrorParser.parseError(e)) }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
