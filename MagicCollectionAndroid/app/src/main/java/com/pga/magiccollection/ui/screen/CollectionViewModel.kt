package com.pga.magiccollection.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.data.local.dao.CollectionWithCount
import com.pga.magiccollection.data.local.entities.CollectionCardEntity
import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.domain.usecase.auth.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.auth.LogoutUseCase
import com.pga.magiccollection.domain.usecase.collection.*
import com.pga.magiccollection.util.ErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val syncCollectionsUseCase: SyncCollectionsUseCase,
    private val checkCollectionNameExistsUseCase: CheckCollectionNameExistsUseCase,
    private val getCollectionByIdUseCase: GetCollectionByIdUseCase,
    private val observeCollectionCardsUseCase: ObserveCollectionCardsUseCase,
    private val observeAllOwnedCardsUseCase: ObserveAllOwnedCardsUseCase,
    private val removeCardFromCollectionUseCase: RemoveCardFromCollectionUseCase,
    private val updateCardInCollectionUseCase: UpdateCardInCollectionUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val observeGlobalCardCountUseCase: ObserveGlobalCardCountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionUiState())
    val uiState: StateFlow<CollectionUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CollectionUiEvent>()
    val events = _events.asSharedFlow()

    private val session = getSessionStateUseCase()
    private var cardsJob: Job? = null

    init {
        if (session.isLoggedIn && session.userId > 0) {
            observeCollections()
            syncCollections()
        }
    }

    fun onAction(action: CollectionUiAction) {
        when (action) {
            is CollectionUiAction.SearchCollections -> onCollectionsSearchQueryChanged(action.query)
            is CollectionUiAction.SearchCards -> onCardsSearchQueryChanged(action.query)
            is CollectionUiAction.ToggleCreateDialog -> showCreateDialog(action.show)
            is CollectionUiAction.ToggleEditDialog -> showEditDialog(action.collection)
            is CollectionUiAction.NameInputChanged -> onNameInputChanged(action.name)
            is CollectionUiAction.CreateCollection -> createCollection()
            is CollectionUiAction.UpdateCollection -> updateCollection()
            is CollectionUiAction.DeleteCollection -> deleteCollection()
            is CollectionUiAction.ClearMessage -> clearMessage()
            is CollectionUiAction.LoadCollectionCards -> loadCollectionCards(action.localId)
            is CollectionUiAction.RemoveCard -> removeCard(action.card)
            is CollectionUiAction.UpdateCardQuantity -> updateCardQuantity(action.card, action.newQuantity)
        }
    }

    fun observeCollections() {
        viewModelScope.launch {
            observeCollectionsUseCase(session.userId).collectLatest { collections ->
                _uiState.update {
                    it.copy(
                        collections = collections,
                        filteredCollections = filterCollections(collections, it.collectionsSearchQuery)
                    )
                }
            }
        }

        viewModelScope.launch {
            observeGlobalCardCountUseCase(session.userId).collectLatest { count ->
                _uiState.update { it.copy(globalCardCount = count) }
            }
        }
    }

    fun onCollectionsSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                collectionsSearchQuery = query,
                filteredCollections = filterCollections(it.collections, query)
            )
        }
    }

    fun onCardsSearchQueryChanged(query: String) {
        _uiState.update {
            it.copy(
                cardsSearchQuery = query,
                filteredCollectionCards = filterCards(it.collectionCards, query)
            )
        }
    }

    fun loadCollectionCards(collectionLocalId: Long) {
        val isAllCollectionsMode = collectionLocalId == ALL_COLLECTIONS_LOCAL_ID
        cardsJob?.cancel()

        if (isAllCollectionsMode) {
            _uiState.update {
                it.copy(
                    selectedCollection = null,
                    isAllCollectionsMode = true,
                    cardsSearchQuery = ""
                )
            }
        } else {
            viewModelScope.launch {
                val collection = getCollectionByIdUseCase(collectionLocalId)
                _uiState.update {
                    it.copy(
                        selectedCollection = collection,
                        isAllCollectionsMode = false,
                        cardsSearchQuery = ""
                    )
                }
            }
        }

        cardsJob = viewModelScope.launch {
            val cardsFlow = if (isAllCollectionsMode) {
                observeAllOwnedCardsUseCase(session.userId)
            } else {
                observeCollectionCardsUseCase(collectionLocalId)
            }

            cardsFlow.collectLatest { cards ->
                val collectionById = _uiState.value.collections.associateBy { it.localId }

                val uiCards = cards.map { card ->
                    OwnedCardUiItem(
                        card = card,
                        collectionName = collectionById[card.collectionLocalId]?.name.orEmpty()
                    )
                }

                _uiState.update {
                    it.copy(
                        collectionCards = uiCards,
                        filteredCollectionCards = filterCards(uiCards, it.cardsSearchQuery)
                    )
                }
            }
        }
    }

    fun removeCard(card: CollectionCardEntity) {
        viewModelScope.launch {
            try {
                removeCardFromCollectionUseCase(card.collectionLocalId, card.localId)
                _uiState.update { it.copy(message = "collection_card_removed_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            }
        }
    }

    fun updateCardQuantity(card: CollectionCardEntity, newQuantity: Int) {
        viewModelScope.launch {
            try {
                updateCardInCollectionUseCase(
                    card.collectionLocalId,
                    card.localId,
                    newQuantity,
                    card.foil,
                    card.language,
                    card.condition
                )
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            }
        }
    }

    fun showCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show, nameInput = "", nameError = null) }
    }

    fun showEditDialog(collection: CollectionEntity?) {
        _uiState.update {
            it.copy(
                showEditDialog = collection != null,
                editingCollection = collection,
                nameInput = collection?.name ?: "",
                nameError = null
            )
        }
    }

    fun onNameInputChanged(name: String) {
        _uiState.update { it.copy(nameInput = name, nameError = null) }
    }

    fun createCollection() {
        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "collection_name_empty_error") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val exists = checkCollectionNameExistsUseCase(name, session.userId)
                if (exists) {
                    _uiState.update { it.copy(nameError = "collection_name_exists_error", isLoading = false) }
                    return@launch
                }
                createCollectionUseCase(name, session.userId)
                _uiState.update {
                    it.copy(
                        showCreateDialog = false,
                        nameInput = "",
                        message = "collection_created_success"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateCollection() {
        val collection = _uiState.value.editingCollection ?: return
        val newName = _uiState.value.nameInput.trim()

        if (newName.isBlank()) {
            _uiState.update { it.copy(nameError = "collection_name_empty_error") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (!newName.equals(collection.name, ignoreCase = true)) {
                    val exists = checkCollectionNameExistsUseCase(newName, session.userId)
                    if (exists) {
                        _uiState.update { it.copy(nameError = "collection_name_exists_error", isLoading = false) }
                        return@launch
                    }
                }
                updateCollectionUseCase(collection.localId, newName)
                _uiState.update {
                    it.copy(
                        showEditDialog = false,
                        editingCollection = null,
                        nameInput = "",
                        message = "collection_updated_success"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteCollection() {
        val collection = _uiState.value.editingCollection ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteCollectionUseCase(collection.localId)
                _uiState.update {
                    it.copy(
                        showEditDialog = false,
                        editingCollection = null,
                        message = "collection_deleted_success"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun syncCollections() {
        viewModelScope.launch {
            try {
                syncCollectionsUseCase(session.userId)
            } catch (_: Exception) {
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun handleError(e: Exception): String {
        if (ErrorParser.isSessionError(e)) {
            logoutUseCase()
            _uiState.update { it.copy(isSessionExpired = true) }
            viewModelScope.launch { _events.emit(CollectionUiEvent.NavigateToLogin) }
        }
        return ErrorParser.parseError(e)
    }

    private fun filterCollections(collections: List<CollectionWithCount>, query: String): List<CollectionWithCount> {
        if (query.isBlank()) {
            return collections
        }
        return collections.filter { it.name.contains(query, ignoreCase = true) }
    }

    private fun filterCards(cards: List<OwnedCardUiItem>, query: String): List<OwnedCardUiItem> {
        if (query.isBlank()) {
            return cards
        }
        return cards.filter {
            it.card.name.contains(query, ignoreCase = true) ||
                    it.card.scryfallId.contains(query, ignoreCase = true) ||
                    it.collectionName.contains(query, ignoreCase = true)
        }
    }
}
