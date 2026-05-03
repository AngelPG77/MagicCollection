package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.entities.CollectionCardEntity
import com.pga.magiccollection.domain.model.enums.CardCondition
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    viewModel: CollectionViewModel,
    mainViewModel: MainViewModel,
    collectionLocalId: Long,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddCard: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(collectionLocalId) {
        viewModel.loadCollectionCards(collectionLocalId)
    }

    val listName = if (uiState.isAllCollectionsMode) {
        stringResource(id = R.string.collection_all_title)
    } else {
        uiState.selectedCollection?.name
    }
    
    LaunchedEffect(listName) {
        if (listName != null) {
            mainViewModel.setTopBarTitle(listName)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.setTopBarTitle(null)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            val displayMsg = if (msg.contains("collection_")) {
                val resId = context.resources.getIdentifier(msg, "string", context.packageName)
                if (resId != 0) context.getString(resId) else msg
            } else msg
            snackbarHostState.showSnackbar(displayMsg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { MagicCollectionSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!uiState.isAllCollectionsMode) {
                FloatingActionButton(
                    onClick = onNavigateToAddCard,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.collection_add_card))
                }
            }
        }
    ) { padding ->
        val innerPadding = PaddingValues(
            start = padding.calculateStartPadding(LayoutDirection.Ltr),
            end = padding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = padding.calculateBottomPadding()
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Cards Search Bar
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.cardsSearchQuery,
                    onValueChange = viewModel::onCardsSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 8.dp),
                    placeholder = { Text(stringResource(id = R.string.collection_card_search_hint)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.cardsSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onCardsSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true
                )
            }

            if (uiState.collectionCards.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.collection_detail_empty),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredCollectionCards, key = { it.card.localId }) { item ->
                        CollectionCardItem(
                            item = item,
                            onClick = { onNavigateToDetail(item.card.scryfallId) },
                            onRemove = { viewModel.removeCard(item.card) },
                            onQuantityChanged = { newQuantity -> viewModel.updateCardQuantity(item.card, newQuantity) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CollectionCardItem(
    item: OwnedCardUiItem,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    onQuantityChanged: (Int) -> Unit
) {
    val card = item.card
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Card Image
            Card(
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(0.718f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(card.imageUrl)
                        .size(160, 223)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build(),
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Card Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!card.typeLine.isNullOrBlank()) {
                    Text(
                        text = card.typeLine,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                if (item.collectionName.isNotEmpty()) {
                    Text(
                        text = item.collectionName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(text = card.language.uppercase(), style = MaterialTheme.typography.labelSmall) }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text(text = card.condition.replace("_", " "), style = MaterialTheme.typography.labelSmall) }
                    )
                    if (card.foil) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text(text = stringResource(id = R.string.wantlist_card_foil), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
            
            // Quantity and Remove
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (card.quantity > 1) onQuantityChanged(card.quantity - 1) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = card.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { onQuantityChanged(card.quantity + 1) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.action_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
