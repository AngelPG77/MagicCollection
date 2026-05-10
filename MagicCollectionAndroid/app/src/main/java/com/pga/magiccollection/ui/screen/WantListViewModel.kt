package com.pga.magiccollection.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.data.local.entities.WantListCardEntity
import com.pga.magiccollection.data.local.entities.WantListEntity
import com.pga.magiccollection.domain.usecase.auth.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.auth.LogoutUseCase
import com.pga.magiccollection.domain.usecase.wantlist.*
import com.pga.magiccollection.util.ErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WantListUiState(
    val wantLists: List<com.pga.magiccollection.data.local.dao.WantListWithCount> = emptyList(),
    val filteredWantLists: List<com.pga.magiccollection.data.local.dao.WantListWithCount> = emptyList(),
    val searchQuery: String = "",
    val selectedWantList: WantListEntity? = null,
    val selectedWantListCards: List<WantListCardEntity> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingWantList: WantListEntity? = null,
    val editingCard: WantListCardEntity? = null,
    val showEditCardModal: Boolean = false,
    val editQuantity: Int = 1,
    val editFoil: Boolean = false,
    val editCondition: String = "NEAR_MINT",
    val editLanguage: String = "en",
    val isSavingCard: Boolean = false,
    val nameInput: String = "",
    val nameError: String? = null,
    val isSessionExpired: Boolean = false
)

@HiltViewModel
class WantListViewModel @Inject constructor(
    private val observeWantListsUseCase: ObserveWantListsUseCase,
    private val observeWantListCardsUseCase: ObserveWantListCardsUseCase,
    private val createWantListUseCase: CreateWantListUseCase,
    private val updateWantListUseCase: UpdateWantListUseCase,
    private val deleteWantListUseCase: DeleteWantListUseCase,
    private val addCardToWantListUseCase: AddCardToWantListUseCase,
    private val removeCardFromWantListUseCase: RemoveCardFromWantListUseCase,
    private val syncWantListsUseCase: SyncWantListsUseCase,
    private val getWantListByIdUseCase: GetWantListByIdUseCase,
    private val checkWantListNameExistsUseCase: CheckWantListNameExistsUseCase,
    private val updateCardInWantListUseCase: UpdateCardInWantListUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WantListUiState())
    val uiState: StateFlow<WantListUiState> = _uiState.asStateFlow()

    private val session = getSessionStateUseCase()

    init {
        if (session.isLoggedIn && session.userId > 0) {
            observeWantLists()
            syncWantLists()
        }
    }

    private fun observeWantLists() {
        viewModelScope.launch {
            observeWantListsUseCase(session.userId).collect { wantLists ->
                _uiState.update { 
                    it.copy(
                        wantLists = wantLists,
                        filteredWantLists = filterLists(wantLists, it.searchQuery)
                    ) 
                }
            }
        }
    }

    private fun filterLists(lists: List<com.pga.magiccollection.data.local.dao.WantListWithCount>, query: String): List<com.pga.magiccollection.data.local.dao.WantListWithCount> {
        if (query.isBlank()) return lists
        return lists.filter { it.name.contains(query, ignoreCase = true) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { 
            it.copy(
                searchQuery = query,
                filteredWantLists = filterLists(it.wantLists, query)
            )
        }
    }

    fun loadWantListCards(wantListLocalId: Long) {
        viewModelScope.launch {
            val wantList = getWantListByIdUseCase(wantListLocalId)
            _uiState.update { it.copy(selectedWantList = wantList) }
            
            observeWantListCardsUseCase(wantListLocalId).collect { cards ->
                _uiState.update { it.copy(selectedWantListCards = cards) }
            }
        }
    }

    fun showCreateDialog(show: Boolean) {
        _uiState.update { it.copy(showCreateDialog = show, nameInput = "", nameError = null) }
    }

    fun showEditDialog(wantList: WantListEntity?) {
        _uiState.update { 
            it.copy(
                showEditDialog = wantList != null, 
                editingWantList = wantList,
                nameInput = wantList?.name ?: "",
                nameError = null
            ) 
        }
    }

    fun onNameInputChanged(name: String) {
        _uiState.update { it.copy(nameInput = name, nameError = null) }
    }

    fun createWantList() {
        val name = _uiState.value.nameInput.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(nameError = "wantlist_name_empty_error") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val exists = checkWantListNameExistsUseCase(name, session.userId)
                if (exists) {
                    _uiState.update { it.copy(nameError = "wantlist_name_exists_error", isLoading = false) }
                    return@launch
                }
                
                createWantListUseCase(name, session.userId)
                _uiState.update { it.copy(showCreateDialog = false, nameInput = "", message = "wantlist_created_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateWantList() {
        val wantList = _uiState.value.editingWantList ?: return
        val newName = _uiState.value.nameInput.trim()
        
        if (newName.isBlank()) {
            _uiState.update { it.copy(nameError = "wantlist_name_empty_error") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                if (newName != wantList.name) {
                    val exists = checkWantListNameExistsUseCase(newName, session.userId)
                    if (exists) {
                        _uiState.update { it.copy(nameError = "wantlist_name_exists_error", isLoading = false) }
                        return@launch
                    }
                }
                
                updateWantListUseCase(wantList.localId, newName)
                _uiState.update { it.copy(showEditDialog = false, editingWantList = null, nameInput = "", message = "wantlist_updated_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(nameError = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteWantList() {
        val wantList = _uiState.value.editingWantList ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                deleteWantListUseCase(wantList.localId)
                _uiState.update { it.copy(showEditDialog = false, editingWantList = null, message = "wantlist_deleted_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addCard(
        scryfallId: String,
        name: String,
        typeLine: String?,
        manaCost: String?,
        imageUrl: String?,
        quantity: Int = 1,
        foil: Boolean = false,
        language: String = "en",
        condition: String = "NEAR_MINT"
    ) {
        val wantList = _uiState.value.selectedWantList ?: return
        
        viewModelScope.launch {
            try {
                addCardToWantListUseCase(
                    wantList.localId, scryfallId, name, typeLine, manaCost, imageUrl, quantity, foil, language, condition
                )
                _uiState.update { it.copy(message = "wantlist_card_added_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            }
        }
    }

    fun removeCard(cardLocalId: Long) {
        val wantList = _uiState.value.selectedWantList ?: return
        
        viewModelScope.launch {
            try {
                removeCardFromWantListUseCase(wantList.localId, cardLocalId)
                _uiState.update { it.copy(message = "wantlist_card_removed_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            }
        }
    }

    fun showEditCardModal(card: WantListCardEntity?) {
        _uiState.update { 
            it.copy(
                showEditCardModal = card != null,
                editingCard = card,
                editQuantity = card?.quantity ?: 1,
                editFoil = card?.foil ?: false,
                editCondition = card?.condition ?: "NEAR_MINT",
                editLanguage = card?.language ?: "en"
            )
        }
    }

    fun onEditQuantityChanged(q: Int) {
        _uiState.update { it.copy(editQuantity = q) }
    }

    fun onEditFoilChanged(f: Boolean) {
        _uiState.update { it.copy(editFoil = f) }
    }

    fun onEditConditionChanged(c: String) {
        _uiState.update { it.copy(editCondition = c) }
    }

    fun onEditLanguageChanged(l: String) {
        _uiState.update { it.copy(editLanguage = l) }
    }

    fun saveEditedCard() {
        val card = _uiState.value.editingCard ?: return
        val state = _uiState.value
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingCard = true) }
            try {
                updateCardInWantListUseCase(
                    card.localId, state.editQuantity, state.editFoil, state.editLanguage, state.editCondition
                )
                _uiState.update { it.copy(showEditCardModal = false, editingCard = null, message = "wantlist_card_updated_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isSavingCard = false) }
            }
        }
    }

    fun syncWantLists() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                syncWantListsUseCase(session.userId)
                // Success message removed at user request
            } catch (e: Exception) {
                _uiState.update { it.copy(message = handleError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
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
        }
        return ErrorParser.parseError(e)
    }
}
