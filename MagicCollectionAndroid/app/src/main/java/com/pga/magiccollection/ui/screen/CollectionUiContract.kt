package com.pga.magiccollection.ui.screen

import com.pga.magiccollection.data.local.dao.CollectionWithCount
import com.pga.magiccollection.data.local.entities.CollectionCardEntity
import com.pga.magiccollection.data.local.entities.CollectionEntity

const val ALL_COLLECTIONS_LOCAL_ID = 0L

data class OwnedCardUiItem(
    val card: CollectionCardEntity,
    val collectionName: String
)

data class CollectionUiState(
    val collections: List<CollectionWithCount> = emptyList(),
    val filteredCollections: List<CollectionWithCount> = emptyList(),
    val globalCardCount: Int = 0,
    val collectionsSearchQuery: String = "",
    val selectedCollection: CollectionEntity? = null,
    val isAllCollectionsMode: Boolean = false,
    val collectionCards: List<OwnedCardUiItem> = emptyList(),
    val filteredCollectionCards: List<OwnedCardUiItem> = emptyList(),
    val cardsSearchQuery: String = "",
    val isLoading: Boolean = false,
    val message: String? = null,
    val showCreateDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingCollection: CollectionEntity? = null,
    val nameInput: String = "",
    val nameError: String? = null,
    val isSessionExpired: Boolean = false
)

sealed interface CollectionUiAction {
    data class SearchCollections(val query: String) : CollectionUiAction
    data class SearchCards(val query: String) : CollectionUiAction
    data class ToggleCreateDialog(val show: Boolean) : CollectionUiAction
    data class ToggleEditDialog(val collection: CollectionEntity?) : CollectionUiAction
    data class NameInputChanged(val name: String) : CollectionUiAction
    object CreateCollection : CollectionUiAction
    object UpdateCollection : CollectionUiAction
    object DeleteCollection : CollectionUiAction
    object ClearMessage : CollectionUiAction
    data class LoadCollectionCards(val localId: Long) : CollectionUiAction
    data class RemoveCard(val card: CollectionCardEntity) : CollectionUiAction
    data class UpdateCardQuantity(val card: CollectionCardEntity, val newQuantity: Int) : CollectionUiAction
}

sealed interface CollectionUiEvent {
    data class ShowSnackbar(val message: String) : CollectionUiEvent
    object NavigateToLogin : CollectionUiEvent
}
