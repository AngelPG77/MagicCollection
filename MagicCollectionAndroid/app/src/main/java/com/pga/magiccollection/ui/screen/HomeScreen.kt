package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.entities.RecentCardEntity
import com.pga.magiccollection.ui.component.EmptyState

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCollections: () -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentCards by viewModel.recentCards.collectAsStateWithLifecycle(initialValue = emptyList())
    
    var showLoginDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.navigateToDetailEvent.collect { cardName ->
            onNavigateToDetail(cardName)
        }
    }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onLogin = {
                showLoginDialog = false
                onNavigateToLogin()
            },
            onRegister = {
                showLoginDialog = false
                onNavigateToRegister()
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Recent Cards Section
        RecentCardsSection(
            cards = recentCards,
            onCardClick = onNavigateToDetail
        )

        // Utilities Section
        UtilitiesSection(
            isLoggedIn = uiState.isLoggedIn,
            onRandomCard = { viewModel.getRandomCard() },
            onWishlist = onNavigateToWishlist,
            onRules = { /* WIP */ },
            onTrade = { /* WIP */ },
            onShowLoginDialog = { showLoginDialog = true }
        )
    }
}

@Composable
fun LoginRequiredDialog(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(id = R.string.join_us)) },
        text = { Text(text = stringResource(id = R.string.dialog_login_required_message)) },
        confirmButton = {
            Button(onClick = onLogin) {
                Text(text = stringResource(id = R.string.login_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onRegister) {
                Text(text = stringResource(id = R.string.register_now))
            }
        }
    )
}

@Composable
fun RecentCardsSection(
    cards: List<RecentCardEntity>,
    onCardClick: (String) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.recent_cards_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        if (cards.isEmpty()) {
            EmptyState(
                title = stringResource(id = R.string.recent_cards_empty),
                icon = Icons.Default.History
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(cards) { card ->
                    Card(
                        modifier = Modifier
                            .width(120.dp)
                            .height(170.dp)
                            .clickable { onCardClick(card.name) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(card.imageUrl)
                                .size(240, 340)
                                .precision(Precision.INEXACT)
                                .crossfade(false)
                                .build(),
                            contentDescription = card.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UtilitiesSection(
    isLoggedIn: Boolean,
    onRandomCard: () -> Unit,
    onWishlist: () -> Unit,
    onRules: () -> Unit,
    onTrade: () -> Unit,
    onShowLoginDialog: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(id = R.string.utilities_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        // Collections is intentionally absent here — it lives in the bottom navigation
        // bar, and surfacing it twice creates UX noise.
        val buttons = listOf(
            UtilityItem(stringResource(id = R.string.utility_random_card), Icons.Default.Refresh, onRandomCard, true),
            UtilityItem(
                label = stringResource(id = R.string.utility_wishlist),
                icon = Icons.Default.Favorite,
                onClick = { if (isLoggedIn) onWishlist() else onShowLoginDialog() },
                enabled = true
            ),
            UtilityItem(stringResource(id = R.string.utility_rules), Icons.Default.Info, onRules, true),
            UtilityItem(stringResource(id = R.string.utility_trade), Icons.Default.Share, onTrade, true)
        )

        buttons.forEach { item ->
            UtilityButton(
                item = item,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

data class UtilityItem(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val enabled: Boolean
)

@Composable
fun UtilityButton(item: UtilityItem, modifier: Modifier = Modifier) {
    Button(
        onClick = item.onClick,
        enabled = item.enabled,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(item.icon, contentDescription = null)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = item.label,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
