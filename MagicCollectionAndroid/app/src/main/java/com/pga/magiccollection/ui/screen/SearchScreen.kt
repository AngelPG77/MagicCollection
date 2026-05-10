package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState
import com.pga.magiccollection.R
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.ui.component.CardGrid
import com.pga.magiccollection.ui.component.PagedCardGrid
import com.pga.magiccollection.ui.component.ColorLogicDropdown
import com.pga.magiccollection.ui.component.LanguageDropdown
import com.pga.magiccollection.ui.component.SortDropdown
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost

import com.pga.magiccollection.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    gridSize: Int,
    onNavigateToDetail: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val isSuggestionMode = !uiState.isSearchPerformed && uiState.query.isNotBlank()

    var localQuery by remember(uiState.query) { mutableStateOf(uiState.query) }
    var localType by remember(uiState.type) { mutableStateOf(uiState.type) }
    var isSetFieldFocused by remember { mutableStateOf(false) }

    val mtgColors = mapOf(
        "W" to ManaWhite,
        "U" to ManaBlue,
        "B" to ManaBlack,
        "R" to ManaRed,
        "G" to ManaGreen,
        "C" to ManaColorless
    )

    LaunchedEffect(Unit) {
        viewModel.navigateToDetailEvent.collect { cardIdentifier ->
            onNavigateToDetail(cardIdentifier)
        }
    }

    val hasActiveFilters = uiState.selectedColors.isNotEmpty() ||
            uiState.type.isNotBlank() ||
            uiState.selectedRarities.isNotEmpty() ||
            uiState.selectedSetCode != null ||
            uiState.useColorIdentity ||
            uiState.colorLogic != ColorMatchMode.EXACTLY ||
            uiState.sortBy != SearchSortBy.NAME ||
            !uiState.sortAscending

    LaunchedEffect(isSuggestionMode, uiState.searchResults) {
        if (!isSuggestionMode) {
            return@LaunchedEffect
        }
        uiState.searchResults.asSequence()
            .mapNotNull { card -> card.imageUrl }
            .take(20)
            .forEach { url ->
                context.imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(url)
                        .size(360, 502)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build()
                )
            }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
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

                    val filterLabels = remember(uiState.selectedColors, uiState.useColorIdentity, uiState.colorLogic, uiState.confirmedQuery, uiState.confirmedType, uiState.selectedRarities, uiState.selectedSetCode) {
                        buildList {
                            if (uiState.selectedColors.isNotEmpty()) add(uiState.selectedColors.sorted().joinToString(""))
                            if (uiState.useColorIdentity) add("ID")
                            if (uiState.colorLogic != ColorMatchMode.EXACTLY) {
                                add(when (uiState.colorLogic) {
                                    ColorMatchMode.AT_MOST -> "≤"
                                    ColorMatchMode.INCLUDING -> "≥"
                                    ColorMatchMode.EXACTLY -> "="
                                })
                            }
                            if (uiState.confirmedQuery.isNotBlank()) add(uiState.confirmedQuery)
                            if (uiState.confirmedType.isNotBlank()) add(uiState.confirmedType)
                            if (uiState.selectedRarities.isNotEmpty()) {
                                add(uiState.selectedRarities.sorted().joinToString(","))
                            }
                            uiState.selectedSetCode?.let { add(it.uppercase()) }
                        }
                    }

                    if (filterLabels.isNotEmpty()) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            LazyRow(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                items(filterLabels) { label ->
                                    AssistChip(onClick = {}, label = { Text(label) })
                                }
                            }
                            if (hasActiveFilters) {
                                IconButton(onClick = { viewModel.clearFilters() }) {
                                    Icon(Icons.Default.FilterAltOff, contentDescription = stringResource(R.string.action_clear), tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
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
                            if (uiState.isSearchPerformed) {
                                val pagedCards = uiState.pagedSearchResults.collectAsLazyPagingItems()
                                
                                PagedCardGrid(
                                    lazyPagingItems = pagedCards,
                                    gridSize = gridSize,
                                    modifier = Modifier.fillMaxSize(),
                                    onCardClick = { card -> card.scryfallId?.let { onNavigateToDetail(it) } }
                                )

                                if (pagedCards.loadState.refresh is LoadState.Loading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
                            } else {
                                if (uiState.searchResults.isEmpty() && !uiState.isLoading) {
                                    val message = if (uiState.hasIndexData) stringResource(id = R.string.search_no_results) else stringResource(id = R.string.index_preparing)
                                    Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                        if (!uiState.hasIndexData) CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                                        Text(text = message, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
                                    }
                                } else {
                                    CardGrid(
                                        cards = uiState.searchResults,
                                        gridSize = gridSize,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        onCardClick = { card -> card.scryfallId?.let { onNavigateToDetail(it) } }
                                    )
                                }
                                if (uiState.isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                                }
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
                                val selected = color in uiState.selectedColors
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.onColorToggled(color) },
                                    label = {
                                        com.pga.magiccollection.ui.component.manaDrawableFor(color)?.let { drawableId ->
                                            androidx.compose.foundation.Image(
                                                painter = androidx.compose.ui.res.painterResource(id = drawableId),
                                                contentDescription = color,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        } ?: Text(color)
                                    }
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

                        var isTypeFieldFocused by remember { mutableStateOf(false) }
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = stringResource(id = R.string.search_filter_type), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(
                                value = localType,
                                onValueChange = { 
                                    localType = it 
                                    viewModel.onTypeQueryChanged(it)
                                },
                                modifier = Modifier.fillMaxWidth().onFocusChanged { state ->
                                    isTypeFieldFocused = state.isFocused
                                    if (!state.isFocused) viewModel.onTypeConfirmed(localType)
                                },
                                placeholder = { Text(stringResource(id = R.string.search_type_hint_placeholder)) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { viewModel.onTypeConfirmed(localType) })
                            )
                            if (isTypeFieldFocused || localType.isNotBlank()) {
                                Card(modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                                    LazyColumn {
                                        items(uiState.filteredTypes) { type ->
                                            ListItem(headlineContent = { Text(type) }, modifier = Modifier.clickable { 
                                                localType = type
                                                viewModel.onTypeConfirmed(type)
                                                isTypeFieldFocused = false
                                            })
                                        }
                                    }
                                }
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = stringResource(id = R.string.search_filter_edition), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                            OutlinedTextField(
                                value = if (uiState.selectedSetCode != null && uiState.setQuery.isBlank()) "" else uiState.setQuery,
                                onValueChange = viewModel::onSetQueryChanged,
                                modifier = Modifier.fillMaxWidth().onFocusChanged { state -> isSetFieldFocused = state.isFocused },
                                placeholder = { Text(uiState.selectedSetName ?: stringResource(id = R.string.search_filter_edition_hint_placeholder)) },
                                trailingIcon = { if (uiState.selectedSetCode != null) IconButton(onClick = viewModel::onClearSet) { Icon(Icons.Default.Close, contentDescription = null) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Password)
                            )
                            if (isSetFieldFocused || uiState.setQuery.isNotBlank()) {
                                Card(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)) {
                                    LazyColumn {
                                        items(uiState.filteredSets) { mtgSet ->
                                            ListItem(headlineContent = { Text(mtgSet.name) }, supportingContent = { Text(mtgSet.code.uppercase()) }, modifier = Modifier.clickable { viewModel.onSetSelected(mtgSet) })
                                        }
                                    }
                                }
                            }
                        }

                        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("common" to R.string.search_rarity_common, "uncommon" to R.string.search_rarity_uncommon, "rare" to R.string.search_rarity_rare, "mythic" to R.string.search_rarity_mythic).forEach { (rarity, label) ->
                                FilterChip(selected = rarity in uiState.selectedRarities, onClick = { viewModel.onRarityToggled(rarity) }, label = { Text(stringResource(id = label)) })
                            }
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
    }
}
