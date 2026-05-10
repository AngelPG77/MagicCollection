package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost
import com.pga.magiccollection.ui.component.OwnedCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WantListDetailScreen(
    viewModel: WantListViewModel,
    mainViewModel: MainViewModel,
    wantListLocalId: Long,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddCard: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(wantListLocalId) {
        viewModel.loadWantListCards(wantListLocalId)
    }

    val listName = uiState.selectedWantList?.name
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
            val displayMsg = if (msg.contains("wantlist_")) {
                val parts = msg.split("|")
                val resId = context.resources.getIdentifier(parts[0], "string", context.packageName)
                if (resId != 0) {
                    if (parts.size > 1) {
                        val formattedArgs = parts.drop(1).map { it.toIntOrNull() ?: it as Any }.toTypedArray()
                        context.getString(resId, *formattedArgs)
                    } else {
                        context.getString(resId)
                    }
                } else msg
            } else msg
            snackbarHostState.showSnackbar(displayMsg)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { MagicCollectionSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.wantlist_add_card))
            }
        }
    ) { padding ->
        val innerPadding = PaddingValues(
            start = padding.calculateStartPadding(LayoutDirection.Ltr),
            end = padding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = padding.calculateBottomPadding()
        )

        if (uiState.selectedWantListCards.isEmpty()) {
            EmptyState(
                title = stringResource(id = R.string.wantlist_detail_empty),
                icon = Icons.Default.Add,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.selectedWantListCards, key = { it.localId }) { card ->
                    Box(modifier = Modifier.animateItem()) {
                        OwnedCardItem(
                            name = card.name,
                            typeLine = card.typeLine,
                            imageUrl = card.imageUrl,
                            quantity = card.quantity,
                            foil = card.foil,
                            language = card.language,
                            condition = card.condition,
                            manaCost = card.manaCost,
                            onEdit = { viewModel.showEditCardModal(card) },
                            onClick = { onNavigateToDetail(card.scryfallId) }
                        )
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
                uiState.editingCard?.let { viewModel.removeCard(it.localId) }
                viewModel.showEditCardModal(null)
            },
            onDismiss = { viewModel.showEditCardModal(null) }
        )
    }
}
