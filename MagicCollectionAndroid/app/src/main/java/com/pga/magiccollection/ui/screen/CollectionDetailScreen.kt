package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R
import com.pga.magiccollection.ui.component.EditOwnedCardModal
import com.pga.magiccollection.ui.component.EmptyState
import com.pga.magiccollection.ui.component.GuildSearchBar
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost
import com.pga.magiccollection.ui.component.OwnedCardItem

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
        if (listName != null) mainViewModel.setTopBarTitle(listName)
    }

    DisposableEffect(Unit) {
        onDispose { mainViewModel.setTopBarTitle(null) }
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
            GuildSearchBar(
                value = uiState.cardsSearchQuery,
                onValueChange = viewModel::onCardsSearchQueryChanged,
                placeholder = stringResource(id = R.string.collection_card_search_hint),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (uiState.collectionCards.isEmpty()) {
                EmptyState(
                    title = stringResource(id = R.string.collection_detail_empty),
                    icon = Icons.Default.Search,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredCollectionCards, key = { it.card.localId }) { item ->
                        Box(modifier = Modifier.animateItem()) {
                            OwnedCardItem(
                                name = item.card.name,
                                typeLine = item.card.typeLine,
                                imageUrl = item.card.imageUrl,
                                quantity = item.card.quantity,
                                foil = item.card.foil,
                                language = item.card.language,
                                condition = item.card.condition,
                                manaCost = item.card.manaCost,
                                collectionName = if (uiState.isAllCollectionsMode) item.collectionName else null,
                                onEdit = { viewModel.showEditCardModal(item.card) },
                                onClick = { onNavigateToDetail(item.card.scryfallId) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showEditCardModal) {
        EditOwnedCardModal(
            imageUrl = uiState.editingCard?.imageUrl,
            quantity = uiState.editQuantity,
            foil = uiState.editFoil,
            condition = uiState.editCondition,
            language = uiState.editLanguage,
            isSaving = uiState.isSavingCard,
            onQuantityChanged = viewModel::onEditQuantityChanged,
            onFoilChanged = viewModel::onEditFoilChanged,
            onConditionChanged = viewModel::onEditConditionChanged,
            onLanguageChanged = viewModel::onEditLanguageChanged,
            onSave = viewModel::saveEditedCard,
            onDelete = {
                uiState.editingCard?.let { viewModel.removeCard(it) }
                viewModel.showEditCardModal(null)
            },
            onDismiss = { viewModel.showEditCardModal(null) }
        )
    }
}
