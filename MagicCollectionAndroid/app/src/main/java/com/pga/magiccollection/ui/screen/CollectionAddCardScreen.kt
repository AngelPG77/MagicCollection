package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pga.magiccollection.R
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.ui.component.CardGrid
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost
import com.pga.magiccollection.ui.component.CardDetailEntryModal
import com.pga.magiccollection.ui.component.VersionSelectionModal
import com.pga.magiccollection.ui.component.ColorLogicDropdown
import com.pga.magiccollection.ui.component.LanguageDropdown
import com.pga.magiccollection.ui.component.SortDropdown

import com.pga.magiccollection.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CollectionAddCardScreen(
    viewModel: CollectionAddCardViewModel,
    collectionLocalId: Long,
    gridSize: Int,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isSuggestionMode = !uiState.isSearchPerformed && uiState.query.isNotBlank()

    var localQuery by remember(uiState.query) { mutableStateOf(uiState.query) }
    var localType by remember(uiState.type) { mutableStateOf(uiState.type) }
    var isSetFieldFocused by remember { mutableStateOf(false) }

    val hasActiveFilters = uiState.selectedColors.isNotEmpty() ||
            uiState.type.isNotBlank() ||
            uiState.selectedRarities.isNotEmpty() ||
            uiState.selectedSetCode != null ||
            uiState.useColorIdentity ||
            uiState.colorLogic != ColorMatchMode.EXACTLY ||
            uiState.sortBy != SearchSortBy.NAME ||
            !uiState.sortAscending

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            val resId = context.resources.getIdentifier(msg, "string", context.packageName)
            val displayMsg = if (resId != 0) context.getString(resId) else msg
            snackbarHostState.showSnackbar(displayMsg)
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { MagicCollectionSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!isSuggestionMode && !uiState.isSearchPerformed) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onQueryChanged(localQuery)
                        viewModel.onTypeConfirmed(localType)
                        viewModel.performSearch()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.action_search))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = padding.calculateBottomPadding(),
                    top = 8.dp
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(id = R.string.search_filter_name),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            com.pga.magiccollection.ui.component.GuildSearchBar(
                                value = localQuery,
                                onValueChange = {
                                    localQuery = it
                                    viewModel.onQueryChanged(it)
                                },
                                placeholder = stringResource(id = R.string.search_name_hint_placeholder),
                                modifier = Modifier.weight(1f),
                                onFocusChange = { focused ->
                                    if (!focused) viewModel.onQueryConfirmed(localQuery)
                                },
                                onSearch = {
                                    viewModel.onQueryChanged(localQuery)
                                    viewModel.performSearch()
                                },
                                onClear = {
                                    localQuery = ""
                                    viewModel.onClearQuery()
                                }
                            )

                            LanguageDropdown(
                                activeLanguage = uiState.activeLanguage,
                                availableLanguages = uiState.availableLanguages,
                                onLanguageSelected = viewModel::onLanguageSelected
                            )
                        }
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                if (isSuggestionMode || uiState.isSearchPerformed) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SortDropdown(
                                selected = uiState.sortBy,
                                onSelected = viewModel::onSortByChanged,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.onSortAscendingChanged(!uiState.sortAscending) }) {
                                Icon(
                                    imageVector = if (uiState.sortAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = stringResource(R.string.action_sort_direction)
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                uiState.errorMessage != null -> {
                                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                                }
                                uiState.searchResults.isEmpty() && !uiState.isLoading -> {
                                    Text(stringResource(id = R.string.search_no_results), modifier = Modifier.align(Alignment.Center))
                                }
                                else -> {
                                    CardGrid(
                                        cards = uiState.searchResults,
                                        gridSize = gridSize,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        onCardClick = { card -> viewModel.onCardSelected(card) }
                                    )
                                }
                            }
                            if (uiState.isLoading) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(stringResource(id = R.string.search_filter_colors), style = MaterialTheme.typography.titleSmall)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            listOf("W", "U", "B", "R", "G", "C").forEach { color ->
                                com.pga.magiccollection.ui.component.ManaColorToggle(
                                    color = color,
                                    selected = color in uiState.selectedColors,
                                    onClick = { viewModel.onColorToggled(color) }
                                )
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            FilterChip(
                                selected = uiState.useColorIdentity,
                                onClick = { viewModel.onUseColorIdentityChanged(!uiState.useColorIdentity) },
                                label = { Text(stringResource(id = R.string.search_filter_color_identity)) }
                            )
                            ColorLogicDropdown(selected = uiState.colorLogic, onSelected = viewModel::onColorLogicChanged, modifier = Modifier.weight(1f))
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = stringResource(id = R.string.search_filter_type), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(
                                value = localType,
                                onValueChange = { localType = it; viewModel.onTypeQueryChanged(it) },
                                modifier = Modifier.fillMaxWidth().onFocusChanged { if (!it.isFocused) viewModel.onTypeConfirmed(localType) },
                                placeholder = { Text(stringResource(id = R.string.search_type_hint_placeholder)) },
                                singleLine = true
                            )
                        }

                        if (hasActiveFilters) {
                            TextButton(onClick = { viewModel.clearFilters() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                                Text(stringResource(id = R.string.search_clear_filters))
                            }
                        }
                    }
                }
            }
        }

        if (uiState.showVersionModal) {
            VersionSelectionModal(
                versions = uiState.cardVersions,
                isLoading = uiState.isLoadingVersions,
                onVersionSelected = viewModel::onVersionSelected,
                onDismiss = viewModel::onDismissVersionModal
            )
        }

        if (uiState.showDetailModal) {
            CardDetailEntryModal(
                version = uiState.selectedVersion,
                quantity = uiState.quantity,
                foil = uiState.foil,
                condition = uiState.condition,
                language = uiState.detailLanguage,
                isSaving = uiState.isSaving,
                onQuantityChanged = viewModel::onQuantityChanged,
                onFoilChanged = viewModel::onFoilChanged,
                onConditionChanged = viewModel::onConditionChanged,
                onLanguageChanged = viewModel::onLanguageChanged,
                onSave = viewModel::saveCard,
                onDismiss = viewModel::onDismissDetailModal
            )
        }
    }
}
