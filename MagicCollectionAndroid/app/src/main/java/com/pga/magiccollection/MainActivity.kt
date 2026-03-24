package com.pga.magiccollection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.pga.magiccollection.data.local.MagicDatabase
import com.pga.magiccollection.data.local.security.SessionManager
import com.pga.magiccollection.data.remote.AuthTokenInterceptor
import com.pga.magiccollection.data.remote.api.AuthApi
import com.pga.magiccollection.data.remote.api.CardsApi
import com.pga.magiccollection.data.remote.api.CollectionsApi
import com.pga.magiccollection.data.repository.AuthRepository
import com.pga.magiccollection.data.repository.CardSearchRepository
import com.pga.magiccollection.data.repository.CollectionSyncRepository
import com.pga.magiccollection.data.repository.SessionRepository
import com.pga.magiccollection.domain.usecase.CreateLocalCollectionUseCase
import com.pga.magiccollection.domain.usecase.GetSessionStateUseCase
import com.pga.magiccollection.domain.usecase.LoginUseCase
import com.pga.magiccollection.domain.usecase.LogoutUseCase
import com.pga.magiccollection.domain.usecase.ObserveCollectionsUseCase
import com.pga.magiccollection.domain.usecase.RegisterUseCase
import com.pga.magiccollection.domain.usecase.SearchCardsUseCase
import com.pga.magiccollection.domain.usecase.SyncCollectionsUseCase
import com.pga.magiccollection.ui.screen.MainUiState
import com.pga.magiccollection.ui.screen.MainViewModel
import com.pga.magiccollection.ui.theme.MagicCollectionAppTheme
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        buildViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicCollectionAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainRoute(
                        viewModel = viewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    private fun buildViewModelFactory(): ViewModelProvider.Factory {
        val sessionManager = SessionManager(applicationContext)
        val database = MagicDatabaseProvider.get(applicationContext)

        val authApi = RetrofitProvider.createAuthApi()
        val cardsApi = RetrofitProvider.createCardsApi(sessionManager)
        val collectionsApi = RetrofitProvider.createCollectionsApi(sessionManager)

        val authRepository = AuthRepository(
            authApi = authApi,
            userDao = database.userDao(),
            sessionManager = sessionManager
        )
        val cardSearchRepository = CardSearchRepository(cardsApi)
        val collectionSyncRepository = CollectionSyncRepository(
            collectionDao = database.collectionDao(),
            collectionsApi = collectionsApi
        )
        val sessionRepository = SessionRepository(sessionManager)

        return MainViewModel.Factory(
            registerUseCase = RegisterUseCase(authRepository),
            loginUseCase = LoginUseCase(authRepository),
            searchCardsUseCase = SearchCardsUseCase(cardSearchRepository),
            createLocalCollectionUseCase = CreateLocalCollectionUseCase(collectionSyncRepository),
            syncCollectionsUseCase = SyncCollectionsUseCase(collectionSyncRepository),
            observeCollectionsUseCase = ObserveCollectionsUseCase(collectionSyncRepository),
            getSessionStateUseCase = GetSessionStateUseCase(sessionRepository),
            logoutUseCase = LogoutUseCase(sessionRepository)
        )
    }
}

private object MagicDatabaseProvider {
    @Volatile
    private var instance: MagicDatabase? = null

    fun get(context: android.content.Context): MagicDatabase {
        return instance ?: synchronized(this) {
            instance ?: androidx.room.Room.databaseBuilder(
                context.applicationContext,
                MagicDatabase::class.java,
                "magic_collection.db"
            ).build().also { instance = it }
        }
    }
}

private object RetrofitProvider {
    private const val BASE_URL = "http://10.0.2.2:8080/"

    fun createAuthApi(): AuthApi {
        val client = baseClientBuilder().build()
        return retrofit(client).create(AuthApi::class.java)
    }

    fun createCardsApi(sessionManager: SessionManager): CardsApi {
        val client = baseClientBuilder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .build()
        return retrofit(client).create(CardsApi::class.java)
    }

    fun createCollectionsApi(sessionManager: SessionManager): CollectionsApi {
        val client = baseClientBuilder()
            .addInterceptor(AuthTokenInterceptor(sessionManager))
            .build()
        return retrofit(client).create(CollectionsApi::class.java)
    }

    private fun retrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun baseClientBuilder(): OkHttpClient.Builder {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
    }
}

@Composable
private fun MainRoute(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    MainScreen(
        state = state,
        modifier = modifier,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onCollectionNameChanged = viewModel::onCollectionNameChanged,
        onRegister = viewModel::register,
        onLogin = viewModel::login,
        onLogout = viewModel::logout,
        onSearchCards = viewModel::searchCards,
        onCreateLocalCollection = viewModel::createLocalCollection,
        onSyncCollections = viewModel::syncCollections
    )
}

@Composable
private fun MainScreen(
    state: MainUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onCollectionNameChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    onSearchCards: () -> Unit,
    onCreateLocalCollection: () -> Unit,
    onSyncCollections: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("MagicCollection - Offline First Demo")
        HorizontalDivider()

        Text("1) Registro / Login")
        OutlinedTextField(
            value = state.usernameInput,
            onValueChange = onUsernameChanged,
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = state.passwordInput,
            onValueChange = onPasswordChanged,
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = !state.authLoading,
                onClick = onRegister
            ) {
                Text(if (state.authLoading) "Registrando..." else "Registrar")
            }

            Button(
                enabled = !state.authLoading,
                onClick = onLogin
            ) {
                Text(if (state.authLoading) "Entrando..." else "Login")
            }
        }

        if (state.isLoggedIn) {
            Text("Sesion activa: ${state.currentUsername}")
            TextButton(onClick = onLogout) {
                Text("Cerrar sesion")
            }
        }

        state.authMessage?.let { Text(it) }

        HorizontalDivider()
        Text("2) Buscar cartas (API Scryfall via backend)")
        OutlinedTextField(
            value = state.searchQueryInput,
            onValueChange = onSearchQueryChanged,
            label = { Text("Buscar carta (ej: lightning bolt)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            enabled = state.isLoggedIn && !state.searchLoading,
            onClick = onSearchCards
        ) {
            Text(if (state.searchLoading) "Buscando..." else "Buscar cartas")
        }
        state.searchMessage?.let { Text(it) }

        if (state.searchResults.isNotEmpty()) {
            state.searchResults.take(10).forEach { card ->
                Text(
                    text = "- ${card.name} | ${card.typeLine.orEmpty()} | ${card.manaCost.orEmpty()}",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.searchResults.size > 10) {
                Text("Mostrando 10 de ${state.searchResults.size} resultados.")
            }
        }

        HorizontalDivider()
        Text("3) Crear coleccion local (offline)")
        OutlinedTextField(
            value = state.collectionNameInput,
            onValueChange = onCollectionNameChanged,
            label = { Text("Nombre de coleccion") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                enabled = state.isLoggedIn && !state.collectionLoading,
                onClick = onCreateLocalCollection
            ) {
                Text(if (state.collectionLoading) "Guardando..." else "Crear local")
            }

            Button(
                enabled = state.isLoggedIn && !state.syncLoading,
                onClick = onSyncCollections
            ) {
                Text(if (state.syncLoading) "Sincronizando..." else "Sincronizar")
            }
        }

        state.collectionMessage?.let { Text(it) }

        Text("4) Colecciones locales")
        if (state.collections.isEmpty()) {
            Text("No hay colecciones locales para este usuario.")
        } else {
            state.collections.forEach { collection ->
                val syncState = if (collection.synced) "SINCRONIZADA" else "PENDIENTE"
                Text("- ${collection.name} [${syncState}]")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MagicCollectionAppTheme {
        Text("MagicCollection")
    }
}

