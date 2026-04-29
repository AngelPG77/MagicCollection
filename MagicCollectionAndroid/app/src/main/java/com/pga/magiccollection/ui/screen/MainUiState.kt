package com.pga.magiccollection.ui.screen

import com.pga.magiccollection.data.local.entities.CollectionEntity
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.data.remote.dto.IndexVersionDto

data class MainUiState(
    val usernameInput: String = "",
    val passwordInput: String = "",
    val rememberMe: Boolean = false,
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
    val authMessageRes: Int? = null,
    val authMessageArgs: List<String> = emptyList(),
    val searchMessage: String? = null,
    val collectionMessage: String? = null,
    val searchResults: List<ScryfallCardDto> = emptyList(),
    val collections: List<CollectionEntity> = emptyList(),
    val isProfileSynced: Boolean = true,

    // Gestión de cuenta
    val showUpdateUsernameDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val newUsernameInput: String = "",
    val currentPasswordInput: String = "",
    val newPasswordInput: String = "",
    val repeatPasswordInput: String = "",
    val isPasswordVisible: Boolean = false,
    val isCurrentPasswordVisible: Boolean = false,
    val isRepeatPasswordVisible: Boolean = false,

    // Idiomas
    val showDownloadDialog: Boolean = false,
    val selectedLanguageToDownload: String? = null,
    val downloadProgress: Float = 0f,
    val isDownloading: Boolean = false,

    // Actualización de Índices
    val showUpdateDialog: Boolean = false,
    val showForceScanDialog: Boolean = false,
    val indexVersion: IndexVersionDto? = null,
    val isUpdatingIndex: Boolean = false,
    val indexProgress: Float = 0f,
    val isForceScanning: Boolean = false,
    val topBarTitle: String? = null
)
