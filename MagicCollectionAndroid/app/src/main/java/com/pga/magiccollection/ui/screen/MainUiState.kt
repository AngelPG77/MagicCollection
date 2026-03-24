package com.pga.magiccollection.ui.screen

import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto

data class MainUiState(
    val usernameInput: String = "",
    val passwordInput: String = "",
    val searchQueryInput: String = "",
    val collectionNameInput: String = "",
    val isLoggedIn: Boolean = false,
    val currentUserId: Long = -1L,
    val currentUsername: String = "",
    val authLoading: Boolean = false,
    val searchLoading: Boolean = false,
    val collectionLoading: Boolean = false,
    val syncLoading: Boolean = false,
    val authMessage: String? = null,
    val searchMessage: String? = null,
    val collectionMessage: String? = null,
    val searchResults: List<ScryfallCardDto> = emptyList(),
    val collections: List<CollectionEntity> = emptyList()
)

