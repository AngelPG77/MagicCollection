package com.pga.magiccollection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pga.magiccollection.ui.screen.HomeScreen
import com.pga.magiccollection.ui.screen.MainViewModel
import com.pga.magiccollection.ui.screen.SettingsScreen
import com.pga.magiccollection.ui.theme.MagicCollectionAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MagicCollectionActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences by viewModel.preferences.collectAsState(initial = null)
            MagicCollectionAppTheme(darkTheme = preferences?.darkTheme ?: false) {
                MainNavigation(viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val titleRes: Int, val icon: ImageVector) {
    object Home : Screen("home", R.string.title_home, Icons.Default.Home)
    object Search : Screen("search", R.string.title_search, Icons.Default.Search)
    object Collections : Screen("collections", R.string.title_collections, Icons.Default.List)
    object Decks : Screen("decks", R.string.title_decks, Icons.Default.PlayArrow)
    object Scanner : Screen("scanner", R.string.title_scanner, Icons.Default.Add)
    object Settings : Screen("settings", R.string.app_name, Icons.Default.Settings) // Usamos app_name temporalmente
    object Register : Screen("register", R.string.app_name, Icons.Default.AccountCircle)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainItems = listOf(Screen.Home, Screen.Search, Screen.Collections, Screen.Decks, Screen.Scanner)

    Scaffold(
        topBar = {
            val currentScreen = when (currentDestination?.route) {
                Screen.Home.route -> Screen.Home
                Screen.Search.route -> Screen.Search
                Screen.Collections.route -> Screen.Collections
                Screen.Decks.route -> Screen.Decks
                Screen.Scanner.route -> Screen.Scanner
                Screen.Settings.route -> Screen.Settings
                else -> Screen.Home
            }

            TopAppBar(
                title = { Text(stringResource(id = currentScreen.titleRes)) },
                actions = {
                    if (currentScreen != Screen.Settings) {
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Opciones")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
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
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToDetail = { /* TODO */ },
                    onNavigateToWishlist = { /* TODO */ },
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Search.route) { WipScreen(Screen.Search) }
            composable(Screen.Collections.route) { WipScreen(Screen.Collections) }
            composable(Screen.Decks.route) { WipScreen(Screen.Decks) }
            composable(Screen.Scanner.route) { WipScreen(Screen.Scanner) }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = viewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) }
                )
            }
            composable(Screen.Register.route) { WipScreen(Screen.Register) }
        }
    }
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
