package com.pga.magiccollection

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.pga.magiccollection.ui.screen.CollectionAddCardScreen
import com.pga.magiccollection.ui.screen.CollectionAddCardViewModel
import com.pga.magiccollection.ui.screen.CollectionDetailScreen
import com.pga.magiccollection.ui.screen.CollectionScreen
import com.pga.magiccollection.ui.screen.CollectionViewModel
import com.pga.magiccollection.ui.screen.HomeScreen
import com.pga.magiccollection.ui.screen.MainViewModel
import com.pga.magiccollection.ui.screen.RegisterScreen
import com.pga.magiccollection.ui.screen.SettingsScreen
import com.pga.magiccollection.ui.screen.GridSizeScreen
import com.pga.magiccollection.ui.screen.SearchScreen
import com.pga.magiccollection.ui.screen.SearchViewModel
import com.pga.magiccollection.ui.screen.WantListScreen
import com.pga.magiccollection.ui.screen.WantListDetailScreen
import com.pga.magiccollection.ui.screen.WantListViewModel
import com.pga.magiccollection.ui.screen.CardDetailViewModel
import com.pga.magiccollection.ui.screen.CardDetailScreen
import com.pga.magiccollection.ui.screen.WantListAddCardViewModel
import com.pga.magiccollection.ui.screen.WantListAddCardScreen
import com.pga.magiccollection.ui.theme.Guild
import com.pga.magiccollection.ui.theme.MagicCollectionAppTheme
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost
import com.pga.magiccollection.ui.screen.ALL_COLLECTIONS_LOCAL_ID
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class MagicCollectionActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by viewModel.preferences.collectAsState(initial = null)
            
            val activity = this@MagicCollectionActivity
            val localizedContext: android.content.Context = remember(preferences?.appLanguage) {
                val pref = preferences?.appLanguage
                // "system" / null / blank → use the activity context as-is, which already
                // respects the device locale via Android's standard resource resolution.
                // Any other value is an explicit override (e.g. "en", "es").
                if (pref.isNullOrBlank() || pref == "system") {
                    activity
                } else {
                    val locale = Locale.forLanguageTag(pref)
                    val config = Configuration(activity.resources.configuration)
                    config.setLocale(locale)
                    val localized = activity.createConfigurationContext(config)

                    object : android.content.ContextWrapper(activity) {
                        override fun getResources() = localized.resources
                        override fun getAssets() = localized.assets
                        override fun getTheme() = activity.theme
                        override fun getSystemService(name: String): Any? {
                            return if (name == android.content.Context.LAYOUT_INFLATER_SERVICE) {
                                localized.getSystemService(name)
                            } else {
                                super.getSystemService(name)
                            }
                        }
                    }
                }
            }

            // Tie the *system* status/navigation bar appearance to the user's Compose
            // dark-mode preference. Without this, when the system is in light mode but
            // the app is dark, the status bar icons stay dark-on-dark and the launch
            // flash uses the light window_background color.
            val isDark = preferences?.darkTheme ?: false
            LaunchedEffect(isDark) {
                val controller = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = !isDark
                controller.isAppearanceLightNavigationBars = !isDark
            }

            CompositionLocalProvider(LocalContext provides localizedContext) {
                MagicCollectionAppTheme(
                    guild = Guild.fromPreferenceValue(preferences?.themeColor),
                    darkTheme = isDark,
                    dynamicSurfaces = preferences?.dynamicColor ?: true
                ) {
                    MainNavigation(viewModel)
                }
            }
        }
    }
}

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.title_home, Icons.Default.Home)
    object Search : Screen("search", R.string.title_search, Icons.Default.Search)
    object Collections : Screen("collections", R.string.title_collections, Icons.Default.Archive)
    object Decks : Screen("decks", R.string.title_decks, Icons.Default.ContentCopy)
    object Scanner : Screen("scanner", R.string.title_scanner, Icons.Default.PhotoCamera)
    object Settings : Screen("settings", R.string.title_settings, Icons.Default.Settings)
    object Register : Screen("register", R.string.title_register, Icons.Default.AccountCircle)
    object Login : Screen("login", R.string.login_now, Icons.Default.AccountCircle)
    object Guides : Screen("guides", R.string.title_guides, Icons.AutoMirrored.Filled.List)
    object Contact : Screen("contact", R.string.title_contact, Icons.Default.Email)
    object GridSizeSettings : Screen("grid_size_settings", R.string.settings_grid_size, Icons.Default.Menu)
    object CardDetail : Screen("card_detail/{cardIdentifier}", R.string.title_card_detail, Icons.Default.Info)
    object WantLists : Screen("wantlists", R.string.wantlist_title, Icons.Default.Favorite)
    object WantListDetail : Screen("wantlist_detail/{localId}", R.string.wantlist_title, Icons.Default.Favorite)
    object WantListAddCard : Screen("wantlist_add_card/{localId}", R.string.wantlist_add_card, Icons.Default.Add)
    object CollectionDetail : Screen("collection_detail/{localId}", R.string.title_collections, Icons.AutoMirrored.Filled.List)
    object CollectionAddCard : Screen("collection_add_card/{localId}", R.string.title_collections, Icons.Default.Add)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val preferences by viewModel.preferences.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val navigationContext = LocalContext.current
    
    // Diálogo de actualización de índice (Bloqueante si se está actualizando)
    if (uiState.showUpdateDialog && uiState.indexVersion != null) {
        UpdateIndexDialog(
            estimatedSizeMb = uiState.indexVersion!!.estimatedSizeMb,
            isUpdating = uiState.isUpdatingIndex,
            progress = uiState.indexProgress,
            onConfirm = viewModel::performIndexUpdate,
            onDismiss = { if (!uiState.isUpdatingIndex) viewModel.showUpdateDialog(false) }
        )
    }

    // Overlay de carga para escaneo forzado (también bloqueante)
    if (uiState.isForceScanning) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(id = R.string.index_updating)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(id = R.string.msg_wait_minutes))
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { uiState.indexProgress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(uiState.indexProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            },
            confirmButton = {}
        )
    }

    // Observar mensajes globales del MainViewModel
    LaunchedEffect(uiState.authMessage, uiState.authMessageRes) {
        if (uiState.authMessageRes != null) {
            try {
                val message = if (uiState.authMessageArgs.isEmpty()) {
                    navigationContext.getString(uiState.authMessageRes!!)
                } else {
                    navigationContext.getString(uiState.authMessageRes!!, *uiState.authMessageArgs.toTypedArray())
                }
                snackbarHostState.showSnackbar(message)
            } catch (e: Exception) {
                // Si falla el formato, intentamos mostrar al menos el mensaje sin formatear o el mensaje de la excepción
                snackbarHostState.showSnackbar("Error: " + (uiState.authMessage ?: "ResId: ${uiState.authMessageRes}"))
            }
            viewModel.clearAuthMessage()
        } else if (uiState.authMessage != null) {
            snackbarHostState.showSnackbar(uiState.authMessage!!)
            viewModel.clearAuthMessage()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.navigateToLoginEvent.collect {
            if (navController.currentBackStackEntry?.destination?.route != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    launchSingleTop = true
                    popUpTo(navController.graph.findStartDestination().id)
                }
            }
        }
    }

    val mainItems = listOf(Screen.Home, Screen.Search, Screen.Collections, Screen.Decks, Screen.Scanner)
    
    // Determinar pantalla inicial basada en preferencias
    val startDestination = remember(preferences?.startScreen) {
        when (preferences?.startScreen) {
            "search" -> Screen.Search.route
            "collections" -> Screen.Collections.route
            "decks" -> Screen.Decks.route
            "scanner" -> Screen.Scanner.route
            else -> Screen.Home.route
        }
    }

    Scaffold(
        snackbarHost = { MagicCollectionSnackbarHost(snackbarHostState) },
        topBar = {
            val currentScreen = when {
                currentDestination?.route == Screen.Home.route -> Screen.Home
                currentDestination?.route == Screen.Search.route -> Screen.Search
                currentDestination?.route == Screen.Collections.route -> Screen.Collections
                currentDestination?.route == Screen.Decks.route -> Screen.Decks
                currentDestination?.route == Screen.Scanner.route -> Screen.Scanner
                currentDestination?.route == Screen.Settings.route -> Screen.Settings
                currentDestination?.route == Screen.Register.route -> Screen.Register
                currentDestination?.route == Screen.Login.route -> Screen.Login
                currentDestination?.route == Screen.Guides.route -> Screen.Guides
                currentDestination?.route == Screen.Contact.route -> Screen.Contact
                currentDestination?.route == Screen.GridSizeSettings.route -> Screen.GridSizeSettings
                currentDestination?.route?.startsWith("card_detail") == true -> Screen.CardDetail
                currentDestination?.route == Screen.WantLists.route -> Screen.WantLists
                currentDestination?.route?.startsWith("wantlist_detail") == true -> Screen.WantListDetail
                currentDestination?.route?.startsWith("wantlist_add_card") == true -> Screen.WantListAddCard
                currentDestination?.route?.startsWith("collection_detail") == true -> Screen.CollectionDetail
                currentDestination?.route?.startsWith("collection_add_card") == true -> Screen.CollectionAddCard
                else -> Screen.Home
            }

            if (true) { // Re-enable TopAppBar for all screens in this block
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            text = uiState.topBarTitle ?: stringResource(id = currentScreen.titleRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        ) 
                    },
                    navigationIcon = {
                        if (currentScreen == Screen.Settings || currentScreen == Screen.Register || 
                            currentScreen == Screen.Login || currentScreen == Screen.Guides || 
                            currentScreen == Screen.Contact || currentScreen == Screen.GridSizeSettings ||
                            currentScreen == Screen.CardDetail || currentScreen == Screen.WantLists ||
                            currentScreen == Screen.WantListDetail || currentScreen == Screen.WantListAddCard ||
                            currentScreen == Screen.CollectionDetail || currentScreen == Screen.CollectionAddCard) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.action_back))
                            }
                        }
                    },
                    actions = {
                        if (currentScreen == Screen.Home) {
                            IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                                Icon(Icons.Default.Settings, contentDescription = stringResource(id = R.string.action_settings))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        },
        bottomBar = {
            if (currentDestination?.route in mainItems.map { it.route }) {
                NavigationBar {
                    mainItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(stringResource(id = screen.titleRes)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220)) +
                    androidx.compose.animation.slideInHorizontally(
                        initialOffsetX = { it / 12 },
                        animationSpec = androidx.compose.animation.core.tween(280)
                    )
            },
            exitTransition = {
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180))
            },
            popEnterTransition = {
                androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(220))
            },
            popExitTransition = {
                androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(180)) +
                    androidx.compose.animation.slideOutHorizontally(
                        targetOffsetX = { it / 12 },
                        animationSpec = androidx.compose.animation.core.tween(280)
                    )
            }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { identifier -> navController.navigate("card_detail/$identifier") },
                    onNavigateToCollections = { navController.navigate(Screen.Collections.route) },
                    onNavigateToWishlist = { navController.navigate(Screen.WantLists.route) },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Search.route) { 
                val searchViewModel: SearchViewModel = hiltViewModel()
                SearchScreen(
                    viewModel = searchViewModel,
                    gridSize = preferences?.gridSize ?: 3,
                    onNavigateToDetail = { scryfallId -> navController.navigate("card_detail/$scryfallId") }
                )
            }
            composable(Screen.Collections.route) { 
                val collectionViewModel: CollectionViewModel = hiltViewModel()
                CollectionScreen(
                    viewModel = collectionViewModel,
                    mainViewModel = viewModel,
                    isLoggedIn = uiState.isLoggedIn,
                    onNavigateToDetail = { localId -> navController.navigate("collection_detail/$localId") },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(Screen.Decks.route) { WipScreen(Screen.Decks) }
            composable(Screen.Scanner.route) { WipScreen(Screen.Scanner) }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                    onNavigateToGuides = { navController.navigate(Screen.Guides.route) },
                    onNavigateToContact = { navController.navigate(Screen.Contact.route) },
                    onNavigateToGridSize = { navController.navigate(Screen.GridSizeSettings.route) }
                )
            }
            composable(Screen.Register.route) { 
                RegisterScreen(
                    viewModel = viewModel,
                    initialLoginMode = false,
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(Screen.Login.route) { 
                RegisterScreen(
                    viewModel = viewModel,
                    initialLoginMode = true,
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(Screen.Guides.route) { WipScreen(Screen.Guides) }
            composable(Screen.Contact.route) { WipScreen(Screen.Contact) }
            composable(Screen.GridSizeSettings.route) {
                GridSizeScreen(viewModel = viewModel)
            }
            composable(Screen.CardDetail.route) {
                val detailViewModel: CardDetailViewModel = hiltViewModel()
                val card by detailViewModel.card.collectAsState()
                val versions by detailViewModel.versions.collectAsState()
                val isLoading by detailViewModel.isLoading.collectAsState()

                CardDetailScreen(
                    card = card,
                    isLoading = isLoading,
                    versions = versions,
                    onBackClick = { navController.popBackStack() },
                    onVersionClick = { scryfallId -> navController.navigate("card_detail/$scryfallId") }
                )
            }
            composable(Screen.WantLists.route) {
                val wantListViewModel: WantListViewModel = hiltViewModel()
                val isLoggedIn = uiState.isLoggedIn
                
                WantListScreen(
                    viewModel = wantListViewModel,
                    mainViewModel = viewModel,
                    isLoggedIn = isLoggedIn,
                    onNavigateToDetail = { localId -> navController.navigate("wantlist_detail/$localId") },
                    onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                )
            }
            composable(
                route = Screen.WantListDetail.route,
                arguments = listOf(navArgument("localId") { type = NavType.LongType })
            ) { backStackEntry ->
                val localId = backStackEntry.arguments?.getLong("localId") ?: 0L
                val wantListViewModel: WantListViewModel = hiltViewModel()
                
                WantListDetailScreen(
                    viewModel = wantListViewModel,
                    mainViewModel = viewModel,
                    wantListLocalId = localId,
                    onNavigateToDetail = { identifier -> navController.navigate("card_detail/$identifier") },
                    onNavigateToAddCard = { navController.navigate("wantlist_add_card/$localId") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.WantListAddCard.route,
                arguments = listOf(navArgument("localId") { type = NavType.LongType })
            ) { backStackEntry ->
                val localId = backStackEntry.arguments?.getLong("localId") ?: 0L
                val wantListAddCardViewModel: WantListAddCardViewModel = hiltViewModel()
                
                WantListAddCardScreen(
                    viewModel = wantListAddCardViewModel,
                    wantListLocalId = localId,
                    gridSize = preferences?.gridSize ?: 3,
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.CollectionDetail.route,
                arguments = listOf(navArgument("localId") { type = NavType.LongType })
            ) { backStackEntry ->
                val localId = backStackEntry.arguments?.getLong("localId") ?: 0L
                val collectionViewModel: CollectionViewModel = hiltViewModel()
                
                CollectionDetailScreen(
                    viewModel = collectionViewModel,
                    mainViewModel = viewModel,
                    collectionLocalId = localId,
                    onNavigateToDetail = { identifier -> navController.navigate("card_detail/$identifier") },
                    onNavigateToAddCard = { navController.navigate("collection_add_card/$localId") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.CollectionAddCard.route,
                arguments = listOf(navArgument("localId") { type = NavType.LongType })
            ) { backStackEntry ->
                val localId = backStackEntry.arguments?.getLong("localId") ?: 0L
                val collectionAddCardViewModel: CollectionAddCardViewModel = hiltViewModel()
                
                CollectionAddCardScreen(
                    viewModel = collectionAddCardViewModel,
                    collectionLocalId = localId,
                    gridSize = preferences?.gridSize ?: 3,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun UpdateIndexDialog(
    estimatedSizeMb: Float,
    isUpdating: Boolean,
    progress: Float,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = if (isUpdating) ({}) else onDismiss,
        title = { 
            Text(if (isUpdating) stringResource(id = R.string.index_updating) else stringResource(id = R.string.index_update_available)) 
        },
        text = {
            Column {
                if (isUpdating) {
                    Text(stringResource(id = R.string.msg_downloading_database))
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.align(Alignment.End)
                    )
                } else {
                    Text(stringResource(id = R.string.index_update_description, estimatedSizeMb))
                }
            }
        },
        confirmButton = {
            if (!isUpdating) {
                TextButton(onClick = onConfirm) {
                    Text(stringResource(id = R.string.action_update))
                }
            }
        },
        dismissButton = {
            if (!isUpdating) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(id = R.string.action_not_now))
                }
            }
        }
    )
}

@Composable
fun WipScreen(screen: Screen) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.work_in_progress),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.wip_description),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "(${stringResource(id = screen.titleRes)})",
                modifier = Modifier.padding(top = 8.dp),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
