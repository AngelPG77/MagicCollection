package com.pga.magiccollection.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pga.magiccollection.domain.usecase.CreateLocalCollectionUseCase
import com.pga.magiccollection.domain.usecase.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.LoginUseCase
import com.pga.magiccollection.domain.usecase.LogoutUseCase
import com.pga.magiccollection.domain.usecase.ObserveCollectionsUseCase
import com.pga.magiccollection.domain.usecase.RegisterUseCase
import com.pga.magiccollection.domain.usecase.SearchCardsUseCase
import com.pga.magiccollection.domain.usecase.SyncCollectionsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class MainViewModel(
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val searchCardsUseCase: SearchCardsUseCase,
    private val createLocalCollectionUseCase: CreateLocalCollectionUseCase,
    private val syncCollectionsUseCase: SyncCollectionsUseCase,
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var collectionsJob: Job? = null

    init {
        loadSession()
    }

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
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                val message = registerUseCase(
                    username = state.usernameInput.trim(),
                    password = state.passwordInput
                )
                _uiState.update { it.copy(authMessage = message) }
            } catch (httpError: HttpException) {
                _uiState.update { it.copy(authMessage = "Registro fallido (HTTP ${httpError.code()}).") }
            } catch (networkError: IOException) {
                _uiState.update { it.copy(authMessage = "No se pudo conectar al backend.") }
            } catch (validationError: IllegalArgumentException) {
                _uiState.update { it.copy(authMessage = validationError.message) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessage = null) }
            try {
                loginUseCase(
                    username = state.usernameInput.trim(),
                    password = state.passwordInput
                )
                loadSession(successMessage = "Login correcto.")
            } catch (httpError: HttpException) {
                _uiState.update { it.copy(authMessage = "Login fallido (HTTP ${httpError.code()}).") }
            } catch (networkError: IOException) {
                _uiState.update { it.copy(authMessage = "No se pudo conectar al backend.") }
            } catch (validationError: IllegalArgumentException) {
                _uiState.update { it.copy(authMessage = validationError.message) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    fun logout() {
        logoutUseCase()
        collectionsJob?.cancel()
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

    fun searchCards() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(searchLoading = true, searchMessage = null) }
            try {
                val results = searchCardsUseCase(state.searchQueryInput)
                _uiState.update {
                    it.copy(
                        searchResults = results,
                        searchMessage = "Resultados: ${results.size}"
                    )
                }
            } catch (httpError: HttpException) {
                _uiState.update { it.copy(searchMessage = "Busqueda fallida (HTTP ${httpError.code()}).") }
            } catch (networkError: IOException) {
                _uiState.update { it.copy(searchMessage = "No se pudo conectar al backend.") }
            } catch (validationError: IllegalArgumentException) {
                _uiState.update { it.copy(searchMessage = validationError.message) }
            } finally {
                _uiState.update { it.copy(searchLoading = false) }
            }
        }
    }

    fun createLocalCollection() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(collectionLoading = true, collectionMessage = null) }
            try {
                createLocalCollectionUseCase(
                    name = state.collectionNameInput,
                    userId = state.currentUserId
                )
                _uiState.update {
                    it.copy(
                        collectionNameInput = "",
                        collectionMessage = "Coleccion guardada en local (pendiente sync)."
                    )
                }
            } catch (validationError: IllegalArgumentException) {
                _uiState.update { it.copy(collectionMessage = validationError.message) }
            } finally {
                _uiState.update { it.copy(collectionLoading = false) }
            }
        }
    }

    fun syncCollections() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(syncLoading = true, collectionMessage = null) }
            try {
                val synced = syncCollectionsUseCase(state.currentUserId)
                _uiState.update { it.copy(collectionMessage = "Sincronizadas: $synced colecciones.") }
            } catch (httpError: HttpException) {
                _uiState.update { it.copy(collectionMessage = "Sync fallido (HTTP ${httpError.code()}).") }
            } catch (networkError: IOException) {
                _uiState.update {
                    it.copy(collectionMessage = "Sin conexion. Se mantienen pendientes para reintento.")
                }
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

    class Factory(
        private val registerUseCase: RegisterUseCase,
        private val loginUseCase: LoginUseCase,
        private val searchCardsUseCase: SearchCardsUseCase,
        private val createLocalCollectionUseCase: CreateLocalCollectionUseCase,
        private val syncCollectionsUseCase: SyncCollectionsUseCase,
        private val observeCollectionsUseCase: ObserveCollectionsUseCase,
        private val getSessionStateUseCase: GetSessionStateUseCase,
        private val logoutUseCase: LogoutUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                return MainViewModel(
                    registerUseCase = registerUseCase,
                    loginUseCase = loginUseCase,
                    searchCardsUseCase = searchCardsUseCase,
                    createLocalCollectionUseCase = createLocalCollectionUseCase,
                    syncCollectionsUseCase = syncCollectionsUseCase,
                    observeCollectionsUseCase = observeCollectionsUseCase,
                    getSessionStateUseCase = getSessionStateUseCase,
                    logoutUseCase = logoutUseCase
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

