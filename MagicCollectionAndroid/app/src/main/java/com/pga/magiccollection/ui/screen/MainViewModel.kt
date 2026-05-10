package com.pga.magiccollection.ui.screen

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkInfo
import com.pga.magiccollection.R
import com.pga.magiccollection.data.connectivity.NetworkConnectivityObserver
import com.pga.magiccollection.data.local.dao.LanguageIndexStateDao
import com.pga.magiccollection.data.local.security.PreferenceManager
import com.pga.magiccollection.data.worker.DownloadLanguageWorker
import com.pga.magiccollection.data.repository.CardSearchIndexRepository
import com.pga.magiccollection.data.worker.SyncCatalogWorker
import com.pga.magiccollection.data.worker.PrefetchImagesWorker
import com.pga.magiccollection.data.worker.SyncDataWorker
import com.pga.magiccollection.domain.usecase.auth.*
import com.pga.magiccollection.domain.usecase.card.*
import com.pga.magiccollection.domain.usecase.collection.*
import com.pga.magiccollection.domain.usecase.home.*
import com.pga.magiccollection.domain.usecase.settings.*
import com.pga.magiccollection.domain.usecase.wantlist.*
import com.pga.magiccollection.util.ErrorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs
import java.util.concurrent.TimeUnit
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val registerUseCase: RegisterUseCase,
    private val loginUseCase: LoginUseCase,
    private val searchCardsUseCase: SearchCardsUseCase,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val syncCollectionsUseCase: SyncCollectionsUseCase,
    private val observeCollectionsUseCase: ObserveCollectionsUseCase,
    private val getSessionStateUseCase: GetSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getRecentCardsUseCase: GetRecentCardsUseCase,
    private val getAppPreferencesUseCase: GetAppPreferencesUseCase,
    private val updateAppPreferenceUseCase: UpdateAppPreferenceUseCase,
    private val updateUsernameUseCase: UpdateUsernameUseCase,
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val getRandomCardUseCase: GetRandomCardUseCase,
    private val ensureUserExistsUseCase: EnsureUserExistsUseCase,
    private val observeSessionExpiredUseCase: ObserveSessionExpiredUseCase,
    private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
    private val syncWantListsUseCase: SyncWantListsUseCase,
    private val cardSearchIndexRepository: CardSearchIndexRepository,
    private val languageIndexStateDao: LanguageIndexStateDao,
    private val preferenceManager: PreferenceManager,
    private val connectivityObserver: NetworkConnectivityObserver
) : ViewModel() {
    private companion object {
        const val INDEX_PROGRESS_STEP = 0.01f
    }

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()
    
    private val _navigateToDetailEvent = MutableSharedFlow<String>()
    val navigateToDetailEvent = _navigateToDetailEvent.asSharedFlow()
    private val _navigateToLoginEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToLoginEvent = _navigateToLoginEvent.asSharedFlow()

    // Flujos de Datos para Home y Settings
    val recentCards = getRecentCardsUseCase()
    val preferences = getAppPreferencesUseCase()

    private var collectionsJob: Job? = null
    private var syncStatusJob: Job? = null
    private var authJob: Job? = null
    private var syncJob: Job? = null
    private var sessionExpiredJob: Job? = null
    private var downloadJob: Job? = null
    private var connectivityJob: Job? = null
    private var lastIndexProgress = -1f

    init {
        loadSession()
        observeSessionExpired()
        checkIndexVersion()
        observeConnectivity()
    }

    /**
     * Watches the device's network state and, on every offline → online transition,
     * enqueues a [SyncDataWorker] to flush any locally-pending changes that didn't
     * make it to the backend while the connection was down.
     *
     * The first emission (cold start) is treated as the baseline — we don't sync on
     * boot from here because the per-screen ViewModels already do that on `init`.
     * What we care about is *transitions* during a running session.
     */
    private fun observeConnectivity() {
        connectivityJob?.cancel()
        connectivityJob = viewModelScope.launch {
            var previous: Boolean? = null
            connectivityObserver.observe().collect { isConnected ->
                if (previous == false && isConnected) {
                    enqueueBackgroundSync()
                }
                previous = isConnected
            }
        }
    }

    /**
     * Enqueues [SyncDataWorker] with a CONNECTED network constraint. WorkManager
     * itself defers the run until the device has connectivity and retries with
     * exponential backoff on failure — i.e., the worker is the durable counterpart
     * to the in-VM `syncAll()`.
     *
     * Uses ExistingWorkPolicy.KEEP so that piling up triggers (e.g. flapping
     * connectivity) collapses to a single in-flight job.
     */
    private fun enqueueBackgroundSync() {
        val userId = _uiState.value.currentUserId
        if (userId == -1L) return

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncDataWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30L, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            SyncDataWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    private fun checkIndexVersion() {
        viewModelScope.launch {
            try {
                val downloadedLangs = preferenceManager.downloadedLanguages.first().ifEmpty { setOf("en") }
                val report = cardSearchIndexRepository.computeSyncDrift(downloadedLangs + "en")

                if (report.isUpToDate) {
                    return@launch
                }

                val serverVersion = cardSearchIndexRepository.getIndexVersion()
                _uiState.update { it.copy(
                    showUpdateDialog = true,
                    indexVersion = serverVersion,
                    pendingSyncLanguages = report.driftedLanguages
                ) }
            } catch (e: Exception) {
                // Silencioso: si falla la comprobación no molestamos al usuario al arrancar.
            }
        }
    }

    fun performIndexUpdate() {
        val version = _uiState.value.indexVersion ?: return
        val pendingLanguages = _uiState.value.pendingSyncLanguages
        viewModelScope.launch {
            resetIndexProgressThrottle()
            _uiState.update { it.copy(isUpdatingIndex = true, indexProgress = 0f) }
            lastIndexProgress = 0f

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val inputBuilder = Data.Builder()
            if (pendingLanguages.isNotEmpty()) {
                inputBuilder.putString(SyncCatalogWorker.KEY_LANGUAGES, pendingLanguages.joinToString(","))
            }

            val workRequest = OneTimeWorkRequestBuilder<SyncCatalogWorker>()
                .setConstraints(constraints)
                .setInputData(inputBuilder.build())
                .build()
                
            WorkManager.getInstance(context).enqueueUniqueWork(
                SyncCatalogWorker.WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            downloadJob?.cancel()
            downloadJob = viewModelScope.launch {
                WorkManager.getInstance(context)
                    .getWorkInfoByIdFlow(workRequest.id)
                    .collect { workInfo ->
                        if (workInfo != null) {
                            when (workInfo.state) {
                                WorkInfo.State.RUNNING -> {
                                    val progress = workInfo.progress.getFloat(SyncCatalogWorker.KEY_PROGRESS, 0f)
                                    updateIndexProgress(progress)
                                }
                                WorkInfo.State.SUCCEEDED -> {
                                    val steps = workInfo.outputData.getInt(SyncCatalogWorker.KEY_STEPS, 1)
                                    _uiState.update { it.copy(
                                        isUpdatingIndex = false,
                                        indexProgress = 0f,
                                        authMessageRes = R.string.index_updated_count,
                                        authMessageArgs = listOf(steps.toString()),
                                        showUpdateDialog = false,
                                        pendingSyncLanguages = emptyList()
                                    )}
                                    resetIndexProgressThrottle()
                                }
                                WorkInfo.State.FAILED -> {
                                    val error = workInfo.outputData.getString(SyncCatalogWorker.KEY_ERROR) ?: "Unknown Error"
                                    _uiState.update { it.copy(
                                        isUpdatingIndex = false,
                                        indexProgress = 0f,
                                        authMessageRes = R.string.index_update_error,
                                        authMessageArgs = listOf(error),
                                        showUpdateDialog = false
                                    )}
                                    resetIndexProgressThrottle()
                                }
                                else -> {}
                            }
                        }
                    }
            }
        }
    }

    private fun observeSessionExpired() {
        sessionExpiredJob?.cancel()
        sessionExpiredJob = viewModelScope.launch {
            observeSessionExpiredUseCase().collect {
                logout()
                _uiState.update { it.copy(authMessageRes = R.string.msg_session_expired) }
                _navigateToLoginEvent.tryEmit(Unit)
            }
        }
    }

    private fun observeSyncStatus(userId: Long) {
        syncStatusJob?.cancel()
        syncStatusJob = viewModelScope.launch {
            observeSyncStatusUseCase(userId).collect { isSynced ->
                _uiState.update { it.copy(isProfileSynced = isSynced) }
            }
        }
    }

    fun syncAll() {
        syncJob?.cancel()
        val userId = _uiState.value.currentUserId
        if (userId == -1L) return

        syncJob = viewModelScope.launch {
            _uiState.update { it.copy(syncLoading = true, authMessageRes = null) }
            try {
                syncCollectionsUseCase(userId)
                syncWantListsUseCase(userId)
                _uiState.update { it.copy(authMessageRes = R.string.msg_profile_synced) }

                // Proactive Offline-First Pre-fetching
                val workData = Data.Builder()
                    .putLong(PrefetchImagesWorker.KEY_USER_ID, userId)
                    .build()
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
                val prefetchRequest = OneTimeWorkRequestBuilder<PrefetchImagesWorker>()
                    .setInputData(workData)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    PrefetchImagesWorker.WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    prefetchRequest
                )

            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
                // Foreground sync failed — schedule a durable retry that WorkManager
                // will replay once connectivity / backend recover.
                enqueueBackgroundSync()
            } finally {
                _uiState.update { it.copy(syncLoading = false) }
            }
        }
    }

    // --- Control de Diálogos ---
    fun showUpdateUsernameDialog(show: Boolean) {
        _uiState.update { it.copy(showUpdateUsernameDialog = show, newUsernameInput = "", authMessageRes = null, authMessage = null) }
    }

    fun showChangePasswordDialog(show: Boolean) {
        _uiState.update { it.copy(showChangePasswordDialog = show, currentPasswordInput = "", newPasswordInput = "", repeatPasswordInput = "", authMessageRes = null, authMessage = null) }
    }

    fun showDeleteAccountDialog(show: Boolean) {
        _uiState.update { it.copy(showDeleteAccountDialog = show, authMessageRes = null, authMessage = null) }
    }

    fun showDownloadDialog(show: Boolean, langCode: String? = null) {
        _uiState.update { it.copy(showDownloadDialog = show, selectedLanguageToDownload = langCode) }
    }

    fun showUpdateDialog(show: Boolean) {
        _uiState.update { it.copy(showUpdateDialog = show) }
    }

    fun showForceScanDialog(show: Boolean) {
        _uiState.update { it.copy(showForceScanDialog = show) }
    }

    fun forceScanScryfall() {
        viewModelScope.launch {
            resetIndexProgressThrottle()
            _uiState.update { it.copy(isForceScanning = true, showForceScanDialog = false, indexProgress = 0f) }
            lastIndexProgress = 0f
            try {
                val downloadedLangs = preferenceManager.downloadedLanguages.first().ifEmpty { setOf("en") }
                val report = try {
                    cardSearchIndexRepository.computeSyncDrift(downloadedLangs + "en")
                } catch (e: Exception) {
                    _uiState.update { it.copy(
                        authMessageRes = R.string.msg_sync_status_check_failed
                    ) }
                    return@launch
                }

                if (report.isUpToDate) {
                    _uiState.update { it.copy(
                        authMessageRes = R.string.msg_sync_already_up_to_date
                    ) }
                    return@launch
                }

                if (!report.backendInSyncWithScryfall) {
                    cardSearchIndexRepository.forceScanScryfall()
                    _uiState.update { it.copy(
                        authMessageRes = R.string.msg_sync_server_updating
                    ) }
                    return@launch
                }

                cardSearchIndexRepository.syncSets()

                val driftedLanguages = report.driftedLanguages
                driftedLanguages.forEachIndexed { index, lang ->
                    cardSearchIndexRepository.bootstrapIndex(lang) { progress ->
                        val globalProgress = (index.toFloat() + progress) / driftedLanguages.size.toFloat()
                        updateIndexProgress(globalProgress)
                    }
                }

                val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val nowIso = sdf.format(Date())
                preferenceManager.setLastIndexUpdate(nowIso)

                _uiState.update { it.copy(authMessageRes = R.string.msg_sync_completed) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    authMessageRes = R.string.msg_sync_error,
                    authMessageArgs = listOf(handleError(e))
                ) }
            } finally {
                _uiState.update { it.copy(isForceScanning = false, indexProgress = 0f) }
                resetIndexProgressThrottle()
            }
        }
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

    fun updateDynamicColor(enabled: Boolean) {
        viewModelScope.launch { updateAppPreferenceUseCase.setDynamicColor(enabled) }
    }

    fun startLanguageDownload(langCode: String) {
        val workData = Data.Builder()
            .putString(DownloadLanguageWorker.KEY_LANG_CODE, langCode)
            .build()

        val workName = "${DownloadLanguageWorker.WORK_NAME_PREFIX}$langCode"
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val workRequest = OneTimeWorkRequestBuilder<DownloadLanguageWorker>()
            .setInputData(workData)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.KEEP,
            workRequest
        )
        
        observeDownloadProgress(workName)
        
        showDownloadDialog(false)
        _uiState.update { it.copy(
            authMessageRes = R.string.lang_download_init,
            authMessageArgs = listOf(langCode)
        ) }
    }

    private fun observeDownloadProgress(workName: String) {
        downloadJob?.cancel()
        downloadJob = viewModelScope.launch {
            WorkManager.getInstance(context)
                .getWorkInfosForUniqueWorkFlow(workName)
                .collect { workInfos ->
                    val workInfo = workInfos.firstOrNull()
                    if (workInfo != null) {
                        val progress = workInfo.progress.getFloat(DownloadLanguageWorker.KEY_PROGRESS, 0f)
                        _uiState.update { it.copy(
                            downloadProgress = progress,
                            isDownloading = !workInfo.state.isFinished
                        ) }
                        
                        if (workInfo.state.isFinished) {
                            _uiState.update { it.copy(isDownloading = false, downloadProgress = 0f) }
                            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                                _uiState.update { it.copy(authMessageRes = R.string.lang_download_success) }
                            } else if (workInfo.state == WorkInfo.State.FAILED) {
                                val error = workInfo.outputData.getString(DownloadLanguageWorker.KEY_ERROR)
                                _uiState.update { it.copy(
                                    authMessageRes = R.string.lang_download_error,
                                    authMessageArgs = listOf(error ?: context.getString(R.string.unknown_error))
                                ) }
                            }
                        }
                    }
                }
        }
    }

    fun getRandomCard() {
        viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessageRes = null, authMessage = null) }
            try {
                val card = getRandomCardUseCase()
                _navigateToDetailEvent.emit(card.name)
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    // --- Lógica de Usuario ---
    fun onUsernameChanged(value: String) {
        _uiState.update { it.copy(usernameInput = value) }
    }

    fun onPasswordChanged(value: String) {
        _uiState.update { it.copy(passwordInput = value) }
    }

    fun onRememberMeChanged(value: Boolean) {
        _uiState.update { it.copy(rememberMe = value) }
    }

    fun onSearchQueryChanged(value: String) {
        _uiState.update { it.copy(searchQueryInput = value) }
    }

    fun onCollectionNameChanged(value: String) {
        _uiState.update { it.copy(collectionNameInput = value) }
    }

    fun setTopBarTitle(title: String?) {
        _uiState.update { it.copy(topBarTitle = title) }
    }

    fun clearAuthMessage() {
        _uiState.update { it.copy(authMessage = null, authMessageRes = null, authMessageArgs = emptyList()) }
    }

    fun register() {
        authJob?.cancel()
        val state = _uiState.value
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessageRes = null, authMessage = null) }
            try {
                registerUseCase(state.usernameInput.trim(), state.passwordInput)
                loginUseCase(state.usernameInput.trim(), state.passwordInput, false)
                loadSession(successResId = R.string.register_success)
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
            _uiState.update { it.copy(authLoading = true, authMessageRes = null, authMessage = null) }
            try {
                loginUseCase(state.usernameInput.trim(), state.passwordInput, state.rememberMe)
                loadSession(successResId = R.string.login_success)
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
                authMessageRes = null,
                authMessageArgs = emptyList(),
                searchMessage = null,
                collectionMessage = null,
                searchResults = emptyList(),
                collections = emptyList()
            )
        }
    }

    fun syncCollections() {
        syncJob?.cancel()
        val state = _uiState.value
        syncJob = viewModelScope.launch {
            _uiState.update { it.copy(syncLoading = true, collectionMessage = null, authMessageRes = null) }
            try {
                syncCollectionsUseCase(state.currentUserId)
                _uiState.update { it.copy(authMessageRes = R.string.msg_collections_synced) }
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(syncLoading = false) }
            }
        }
    }

    fun deleteUser() {
        authJob?.cancel()
        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessageRes = null, authMessage = null) }
            try {
                deleteUserUseCase()
                logout()
                showDeleteAccountDialog(false)
                _uiState.update { it.copy(authMessageRes = R.string.account_deleted_success) }
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
            _uiState.update { it.copy(authLoading = true, authMessageRes = null, authMessage = null) }
            try {
                updateUsernameUseCase(state.newUsernameInput.trim())
                showUpdateUsernameDialog(false)
                loadSession(successResId = R.string.username_updated_success)
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
            _uiState.update { it.copy(authMessageRes = R.string.error_passwords_dont_match) }
            return
        }

        if (state.currentPasswordInput.isBlank() || state.newPasswordInput.isBlank()) return

        authJob = viewModelScope.launch {
            _uiState.update { it.copy(authLoading = true, authMessageRes = null, authMessage = null) }
            try {
                updatePasswordUseCase(state.currentPasswordInput, state.newPasswordInput)
                showChangePasswordDialog(false)
                _uiState.update { it.copy(authMessageRes = R.string.password_updated_success) }
            } catch (e: Exception) {
                _uiState.update { it.copy(authMessage = handleError(e)) }
            } finally {
                _uiState.update { it.copy(authLoading = false) }
            }
        }
    }

    private fun loadSession(successResId: Int? = null) {
        val session = getSessionStateUseCase()
        _uiState.update {
            it.copy(
                isLoggedIn = session.isLoggedIn,
                currentUserId = session.userId,
                currentUsername = session.username,
                usernameInput = if (it.usernameInput.isBlank()) session.username else it.usernameInput,
                passwordInput = "",
                authMessageRes = successResId
            )
        }
        if (session.isLoggedIn && session.userId > 0) {
            viewModelScope.launch {
                ensureUserExistsUseCase()
                observeCollections(session.userId)
                observeSyncStatus(session.userId)
            }
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
        if (ErrorParser.isSessionError(e)) {
            viewModelScope.launch {
                logoutUseCase()
                loadSession()
            }
        }
        return ErrorParser.parseError(e)
    }

    private fun updateIndexProgress(value: Float) {
        val normalized = value.coerceIn(0f, 1f)
        val shouldEmit = lastIndexProgress < 0f ||
                normalized >= 1f ||
                abs(normalized - lastIndexProgress) >= INDEX_PROGRESS_STEP
        if (shouldEmit) {
            lastIndexProgress = normalized
            _uiState.update { it.copy(indexProgress = normalized) }
        }
    }

    private fun resetIndexProgressThrottle() {
        lastIndexProgress = -1f
    }
}
