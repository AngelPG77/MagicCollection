package com.pga.magiccollection.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.domain.usecase.auth.*
import com.pga.magiccollection.domain.usecase.card.*
import com.pga.magiccollection.domain.usecase.collection.*
import com.pga.magiccollection.domain.usecase.inventory.*
import com.pga.magiccollection.domain.usecase.home.*
import com.pga.magiccollection.domain.usecase.settings.*
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
    private val updateAppPreferenceUseCase: UpdateAppPreferenceUseCase
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

    // --- Preferencias ---
    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch { updateAppPreferenceUseCase.setDarkTheme(enabled) }
    }

    fun updateGridSize(size: Int) {
        viewModelScope.launch { updateAppPreferenceUseCase.setGridSize(size) }
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
                val message = registerUseCase(
                    username = state.usernameInput.trim(),
                    password = state.passwordInput
                )
                _uiState.update { it.copy(authMessage = message) }
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
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    401 -> "No autorizado: credenciales incorrectas."
                    403 -> "Prohibido: no tienes permisos."
                    404 -> "No encontrado."
                    409 -> "Conflicto: el recurso ya existe."
                    else -> "Error del servidor (HTTP ${e.code()})."
                }
            }
            is IOException -> "Sin conexión: verifica tu internet."
            else -> e.message ?: "Ha ocurrido un error inesperado."
        }
    }
}
