package com.pga.magiccollection.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.domain.usecase.auth.*
import com.pga.magiccollection.domain.usecase.card.*
import com.pga.magiccollection.domain.usecase.collection.*
import com.pga.magiccollection.domain.usecase.inventory.*
import com.pga.magiccollection.domain.usecase.home.*
import com.pga.magiccollection.domain.usecase.settings.*
import com.pga.magiccollection.util.ErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val searchCardsUseCase: SearchCardsUseCase,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val syncCollectionsUseCase: SyncCollectionsUseCase,
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getRecentCardsUseCase: GetRecentCardsUseCase,
    private val addRecentCardUseCase: AddRecentCardUseCase,
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase,
    private val updateAppPreferenceUseCase: UpdateAppPreferenceUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val deleteUserUseCase: DeleteUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Flujos de Datos para Home y Settings
    val recentCards = getRecentCardsUseCase()
    val preferences = getAppPreferencesUseCase()

    private var collectionsJob: Job? = null
    private var authJob: Job? = null
    private var syncJob: Job? = null

    init {
        loadSession()
    }

    // --- Control de Diálogos ---
    fun showUpdateUsernameDialog(show: Boolean) {
        _uiState.update { it.copy(showUpdateUsernameDialog = show, newUsernameInput = "", authMessage = null) }
    }

    fun showChangePasswordDialog(show: Boolean) {
        _uiState.update { it.copy(showChangePasswordDialog = show, currentPasswordInput = "", newPasswordInput = "", repeatPasswordInput = "", authMessage = null) }
    }

    fun showDeleteAccountDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteAccountDialog = show, authMessage = null) }
    }

    fun onNewUsernameChanged(value: String) {
        _uiState.update { it.copy(newUsernameInput = value) }
    }

    fun onCurrentPasswordChanged(value: String) {
        _uiState.update { it.copy(currentPasswordInput = value) }
    }

    fun onNewPasswordChanged(value: String) {
        _uiState.update { it.copy(newPasswordInput = value) }
    }

    fun onRepeatPasswordChanged(value: String) {
        _uiState.update { it.copy(repeatPasswordInput = value) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleCurrentPasswordVisibility() {
        _uiState.update { it.copy(isCurrentPasswordVisible = !it.isCurrentPasswordVisible) }
    }

    fun toggleRepeatPasswordVisibility() {
        _uiState.update { it.copy(isRepeatPasswordVisible = !it.isRepeatPasswordVisible) }
    }

    // --- Preferencias ---
    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch { updateAppPreferenceUseCase.setDarkTheme(enabled) }
    }

    fun updateGridSize(size: Int) {
        viewModelScope.launch { updateAppPreferenceUseCase.setGridSize(size) }
    }

    fun updateSearchLanguage(lang: String) {
        viewModelScope.launch { updateAppPreferenceUseCase.setSearchLanguage(lang) }
    }

    fun updateAppLanguage(lang: String) {
        viewModelScope.launch { updateAppPreferenceUseCase.setAppLanguage(lang) }
    }

    fun updateThemeColor(color: String) {
        viewModelScope.launch { updateAppPreferenceUseCase.setThemeColor(color) }
    }

    // --- Lógica de Usuario ---
    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(usernameInput = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value) }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQueryInput = value) }
    }

    fun onCollectionNameChanged(value: String) {
        _uiState.update { it.copy(collectionNameInput = value) }
    }

    fun register() {
        authJob?.cancel()
        val state = _uiState.value
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                registerUseCase(
                    username = state.usernameInput.trim(),
                    password = state.passwordInput
                )
                // Si el registro tiene éxito, hacemos login automático
                loginUseCase(
                    username = state.usernameInput.trim(),
                    password = state.passwordInput
                )
                loadSession(successMessage = "Cuenta creada y sesión iniciada.")
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    fun login() {
        authJob?.cancel()
        val state = _uiState.value
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                loginUseCase(
                    username = state.usernameInput.trim(),
                    password = state.passwordInput
                )
                loadSession(successMessage = "Login correcto.")
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    fun logout() {
        logoutUseCase()
        collectionsJob?.cancel()
        authJob?.cancel()
        syncJob?.cancel()
        _uiState.update {
            it.copy(
                isLoggedIn = false,
                currentUserId = -1L,
                currentUsername = "",
                passwordInput = "",
                authMessage = null,
                searchMessage = null,
                collectionMessage = null,
                searchResults = emptyList(),
                collections = emptyList()
            )
        }
    }

    // --- Colecciones y Sincronización ---
    fun syncCollections() {
        syncJob?.cancel()
        val state = _uiState.value
        syncJob = viewModelScope.launch {
            _uiState.update { it.copy(syncLoading = true, collectionMessage = null) }
            try {
                val synced = syncCollectionsUseCase(state.currentUserId)
                _uiState.update { it.copy(collectionMessage = "Sincronizadas: $synced colecciones.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(collectionMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(syncLoading = false) }
            }
        }
    }

    fun deleteUser() {
        authJob?.cancel()
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                deleteUserUseCase()
                logout()
                showDeleteAccountDialog(false)
                _uiState.update { it.copy(authMessage = "Cuenta eliminada correctamente.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    fun updateUsername() {
        authJob?.cancel()
        val state = _uiState.value
        if (state.newUsernameInput.isBlank()) return
        
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                updateUsernameUseCase(state.newUsernameInput.trim())
                showUpdateUsernameDialog(false)
                loadSession(successMessage = "Nombre de usuario actualizado.")
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    fun updatePassword() {
        authJob?.cancel()
        val state = _uiState.value
        
        if (state.newPasswordInput != state.repeatPasswordInput) {
            _uiState.update { it.copy(authMessage = "Las contraseñas no coinciden") }
            return
        }

        if (state.currentPasswordInput.isBlank() || state.newPasswordInput.isBlank()) return

        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                updatePasswordUseCase(state.currentPasswordInput, state.newPasswordInput)
                showChangePasswordDialog(false)
                _uiState.update { it.copy(authMessage = "Contraseña actualizada correctamente.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    private fun loadSession(successMessage: String? = null) {
        val session = getSessionStateUseCase()
        _uiState.update {
            it.copy(
                isLoggedIn = session.isLoggedIn,
                currentUserId = session.userId,
                currentUsername = session.username,
                usernameInput = if (it.usernameInput.isBlank()) session.username else it.usernameInput,
                passwordInput = "",
                authMessage = successMessage
            )
        }
        if (session.isLoggedIn && session.userId > 0) {
            observeCollections(session.userId)
        }
    }

    private fun observeCollections(userId: Long) {
        collectionsJob?.cancel()
        collectionsJob = viewModelScope.launch {
            observeCollectionsUseCase(userId).collect { collections ->
                _uiState.update { it.copy(collections = collections) }
            }
        }
    }

    private fun handleError(e: Exception): String {
        // Si es error de sesión expirada/inválida, cerrar sesión automáticamente
        if (ErrorParser.isSessionError(e)) {
            viewModelScope.launch {
                logoutUseCase()
                loadSession()
            }
        }
        return ErrorParser.parseError(e)
    }
}
