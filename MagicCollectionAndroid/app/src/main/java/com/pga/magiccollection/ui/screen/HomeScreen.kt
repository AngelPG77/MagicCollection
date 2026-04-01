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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.entities.RecentCardEntity

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToWishlist: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentCards by viewModel.recentCards.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Sección de Cartas Recientes
        RecentCardsSection(
            cards = recentCards,
            onCardClick = onNavigateToDetail
        )

        // Sección de Utilidades
        UtilitiesSection(
            isLoggedIn = uiState.isLoggedIn,
            onRandomCard = { /* TODO: Implement random logic */ },
            onWishlist = onNavigateToWishlist,
            onRules = { /* WIP */ },
            onTrade = { /* WIP */ }
        )
    }
}

@Composable
fun RecentCardsSection(
    cards: List<RecentCardEntity>,
    onCardClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(id = R.string.recent_cards_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (cards.isEmpty()) {
            Text(
                text = stringResource(id = R.string.recent_cards_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
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
                            .clickable { onCardClick(card.scryfallId) },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = card.imageUrl,
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
    onTrade: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(id = R.string.utilities_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        val buttons = listOf(
            UtilityItem(stringResource(id = R.string.utility_random_card), Icons.Default.Refresh, onRandomCard, true),
            UtilityItem(stringResource(id = R.string.utility_wishlist), Icons.Default.Favorite, onWishlist, isLoggedIn),
            UtilityItem(stringResource(id = R.string.utility_rules), Icons.Default.Info, onRules, true),
            UtilityItem(stringResource(id = R.string.utility_trade), Icons.Default.Share, onTrade, true)
        )

        buttons.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    UtilityButton(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
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
        modifier = modifier.height(80.dp),
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(item.icon, contentDescription = null)
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
