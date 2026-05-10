package com.pga.magiccollection.ui.screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.domain.usecase.card.GetCardByNameUseCase
import com.pga.magiccollection.domain.usecase.card.GetCardByScryfallIdUseCase
import com.pga.magiccollection.domain.usecase.card.SearchCardsUseCase
import com.pga.magiccollection.domain.usecase.home.AddRecentCardUseCase
import com.pga.magiccollection.domain.usecase.settings.GetAppPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardDetailViewModel @Inject constructor(
    private val getCardByNameUseCase: GetCardByNameUseCase,
    private val getCardByScryfallIdUseCase: GetCardByScryfallIdUseCase,
    private val searchCardsUseCase: SearchCardsUseCase,
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase,
    private val addRecentCardUseCase: AddRecentCardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cardIdentifier: String = checkNotNull(savedStateHandle["cardIdentifier"])

    private val _card = MutableStateFlow<ScryfallCardDto?>(null)
    val card: StateFlow<ScryfallCardDto?> = _card.asStateFlow()

    private val _versions = MutableStateFlow<List<ScryfallCardDto>>(emptyList())
    val versions: StateFlow<List<ScryfallCardDto>> = _versions.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCardDetails()
    }

    private fun loadCardDetails() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val preferences = getAppPreferencesUseCase().first()
                val lang = preferences.searchLanguage // Use search language, not app language
                
                // Determine if identifier is a UUID (Scryfall ID) or a Name
                val isUuid = cardIdentifier.matches(Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$"))
                
                val cardDto = if (isUuid) {
                    getCardByScryfallIdUseCase(cardIdentifier, lang)
                } else {
                    getCardByNameUseCase(cardIdentifier, lang)
                }
                
                _card.value = cardDto

                // Record the visit so the card shows up in Home's "recent cards" carousel.
                // We pick a reasonable thumbnail size (small → normal fallback) and skip
                // the insert silently if the DTO is missing a scryfallId (shouldn't happen
                // in practice but the entity primary key is non-null).
                cardDto.scryfallId?.takeIf { it.isNotBlank() }?.let { id ->
                    addRecentCardUseCase(
                        scryfallId = id,
                        name = cardDto.printedName ?: cardDto.name,
                        imageUrl = cardDto.imageUris?.small
                            ?: cardDto.imageUris?.normal
                            ?: cardDto.imageUris?.large
                    )
                }

                // Try to load versions using exact name search and unique prints
                try {
                    val versionsQuery = "!\"${cardDto.name}\" include:extras unique:prints"
                    val versionsResult = searchCardsUseCase(query = versionsQuery, lang = lang)
                    _versions.value = versionsResult
                } catch (e: Exception) {
                    // Ignore versions error for now
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }
}
