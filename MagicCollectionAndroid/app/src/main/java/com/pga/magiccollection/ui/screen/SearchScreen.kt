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
import androidx.compose.ui.text.style.TextOverflow
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.ui.component.CardGrid

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

    // Estados locales para el input "en curso" antes del onBlur
    var localQuery by remember(uiState.query) { mutableStateOf(uiState.query) }
    var localType by remember(uiState.type) { mutableStateOf(uiState.type) }
    var isSetFieldFocused by remember { mutableStateOf(false) }

    // Definición de colores de Magic
    val mtgColors = mapOf(
        "W" to Color(0xFFFFFBD5), // Blanco/Crema
        "U" to Color(0xFFAAE0FA), // Azul
        "B" to Color(0xFF121212), // Negro
        "R" to Color(0xFFF9AA8F), // Rojo
        "G" to Color(0xFF9BD3AE), // Verde
        "C" to Color(0xFFCCCCCC)  // Incoloro
    )

    // Observar eventos de navegación desde el ViewModel
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

    val filterLabels = remember(
        uiState.selectedColors,
        uiState.useColorIdentity,
        uiState.colorLogic,
        uiState.confirmedQuery,
        uiState.confirmedType,
        uiState.selectedRarities,
        uiState.selectedSetCode
    ) {
        buildList {
            if (uiState.selectedColors.isNotEmpty()) add(uiState.selectedColors.sorted().joinToString(""))
            if (uiState.useColorIdentity) add("ID")
            if (uiState.colorLogic != ColorMatchMode.EXACTLY) {
                add(
                    when (uiState.colorLogic) {
                        ColorMatchMode.AT_MOST -> "≤"
                        ColorMatchMode.INCLUDING -> "≥"
                        ColorMatchMode.EXACTLY -> "="
                    }
                )
            }
            if (uiState.confirmedQuery.isNotBlank()) add(uiState.confirmedQuery)
            if (uiState.confirmedType.isNotBlank()) add(uiState.confirmedType)
            if (uiState.selectedRarities.isNotEmpty()) {
                add(uiState.selectedRarities.sorted().joinToString(","))
            }
            uiState.selectedSetCode?.let {
                add(it.uppercase())
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Cabecera fija con el buscador de texto
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
                        OutlinedTextField(
                            value = localQuery,
                            onValueChange = { 
                                localQuery = it 
                                viewModel.onQueryChanged(it)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { 
                                    if (!it.isFocused) {
                                        viewModel.onQueryConfirmed(localQuery)
                                    }
                                },
                            placeholder = { Text(stringResource(id = R.string.search_name_hint_placeholder)) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (localQuery.isNotEmpty()) {
                                    IconButton(onClick = { 
                                        localQuery = ""
                                        viewModel.onClearQuery() 
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = stringResource(id = R.string.desc_clear_text)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Unspecified,
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.onQueryChanged(localQuery)
                                    viewModel.performSearch()
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        LanguageDropdown(
                            activeLanguage = uiState.activeLanguage,
                            availableLanguages = uiState.availableLanguages,
                            onLanguageSelected = viewModel::onLanguageSelected
                        )
                    }
                }

                if (filterLabels.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LazyRow(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items(filterLabels) { label ->
                                AssistChip(
                                    onClick = {},
                                    label = { Text(label) }
                                )
                            }
                        }
                        if (hasActiveFilters) {
                            IconButton(onClick = { viewModel.clearFilters() }) {
                                Icon(
                                    imageVector = Icons.Default.FilterAltOff,
                                    contentDescription = stringResource(id = R.string.desc_clear_all_filters),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Cuerpo: Filtros o Resultados
        Box(modifier = Modifier.fillMaxSize()) {
            if (isSuggestionMode || uiState.isSearchPerformed) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Opciones de ordenación solo en la parte superior de resultados/sugerencias
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
                                    contentDescription = null
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                uiState.errorMessage != null -> {
                                    Text(
                                        text = uiState.errorMessage ?: "",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                uiState.searchResults.isEmpty() && !uiState.isLoading -> {
                                    Text(
                                        text = stringResource(id = R.string.search_no_results),
                                        modifier = Modifier.align(Alignment.Center)
                                    )
                                }

                                else -> {
                                    CardGrid(
                                        cards = uiState.searchResults,
                                        gridSize = gridSize,
                                        modifier = Modifier.fillMaxSize(),
                                        contentPadding = PaddingValues(8.dp),
                                        onCardClick = { card -> onNavigateToDetail(card.scryfallId) }
                                    )
                                }
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
                    Text(
                        text = stringResource(id = R.string.search_filter_colors),
                        style = MaterialTheme.typography.titleSmall
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val colors = listOf("W", "U", "B", "R", "G", "C")
                        colors.forEach { colorKey ->
                            val isSelected = colorKey in uiState.selectedColors
                            val baseColor = mtgColors[colorKey] ?: Color.Gray
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.onColorToggled(colorKey) },
                                label = { 
                                    Text(
                                        text = colorKey,
                                        color = if (colorKey == "B") Color.White else Color.Unspecified
                                    ) 
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = baseColor.copy(alpha = 0.6f),
                                    selectedContainerColor = baseColor.copy(alpha = 1.0f),
                                    labelColor = if (colorKey == "B") Color.White else MaterialTheme.colorScheme.onSurface,
                                    selectedLabelColor = if (colorKey == "B") Color.White else MaterialTheme.colorScheme.onSurface
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = Color.Transparent,
                                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                                    selectedBorderWidth = 2.dp
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = uiState.useColorIdentity,
                            onClick = { viewModel.onUseColorIdentityChanged(!uiState.useColorIdentity) },
                            label = { Text(stringResource(id = R.string.search_filter_color_identity)) }
                        )

                        ColorLogicDropdown(
                            selected = uiState.colorLogic,
                            onSelected = viewModel::onColorLogicChanged,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Filtro de Tipo con sugerencias
                    var isTypeFieldFocused by remember { mutableStateOf(false) }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.search_filter_type),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = localType,
                            onValueChange = { 
                                localType = it 
                                viewModel.onTypeQueryChanged(it)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    isTypeFieldFocused = state.isFocused
                                    if (!state.isFocused) {
                                        viewModel.onTypeConfirmed(localType)
                                    }
                                },
                            placeholder = { Text(stringResource(id = R.string.search_type_hint_placeholder)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { viewModel.onTypeConfirmed(localType) })
                        )
                        
                        if (isTypeFieldFocused || localType.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 200.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                LazyColumn {
                                    items(uiState.filteredTypes) { type ->
                                        ListItem(
                                            headlineContent = { Text(type) },
                                            modifier = Modifier.clickable { 
                                                localType = type
                                                viewModel.onTypeChanged(type)
                                                isTypeFieldFocused = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Sección de Edición / Expansión
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = stringResource(id = R.string.search_filter_edition),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        OutlinedTextField(
                            value = if (uiState.selectedSetCode != null && uiState.setQuery.isBlank()) 
                                "" 
                            else 
                                uiState.setQuery,
                            onValueChange = viewModel::onSetQueryChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state -> isSetFieldFocused = state.isFocused },
                            placeholder = { 
                                Text(uiState.selectedSetName ?: stringResource(id = R.string.search_filter_edition_hint_placeholder)) 
                            },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                            trailingIcon = {
                                if (uiState.selectedSetCode != null) {
                                    IconButton(onClick = viewModel::onClearSet) {
                                        Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.desc_clear_set))
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                autoCorrectEnabled = false,
                                keyboardType = KeyboardType.Password
                            )
                        )

                        if (isSetFieldFocused || uiState.setQuery.isNotBlank()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(uiState.filteredSets) { mtgSet ->
                                        ListItem(
                                            headlineContent = { 
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Surface(
                                                        color = MaterialTheme.colorScheme.primaryContainer,
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = mtgSet.code.uppercase(),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = mtgSet.name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            },
                                            modifier = Modifier.clickable { viewModel.onSetSelected(mtgSet) }
                                        )
                                        HorizontalDivider(
                                            thickness = 0.5.dp, 
                                            color = MaterialTheme.colorScheme.outlineVariant,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val rarities = listOf(
                            "common" to R.string.search_rarity_common,
                            "uncommon" to R.string.search_rarity_uncommon,
                            "rare" to R.string.search_rarity_rare,
                            "mythic" to R.string.search_rarity_mythic
                        )
                        rarities.forEach { (rarity, label) ->
                            val selected = rarity in uiState.selectedRarities
                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.onRarityToggled(rarity) },
                                label = { Text(stringResource(id = label)) }
                            )
                        }
                    }

                    if (hasActiveFilters) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(id = R.string.search_clear_filters))
                        }
                    }
                }
            }

            // Botón flotante siempre visible
            FloatingActionButton(
                onClick = { 
                    viewModel.onQueryChanged(localQuery)
                    viewModel.onTypeConfirmed(localType)
                    viewModel.performSearch() 
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(id = R.string.title_search)
                )
            }
        }
    }
}

@Composable
private fun LanguageDropdown(
    activeLanguage: String,
    availableLanguages: List<String>,
    onLanguageSelected: (String) -> Unit
) {
    val canExpand = availableLanguages.size > 1
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { if (canExpand) expanded = true }) {
            Text(activeLanguage.uppercase(), fontWeight = FontWeight.Bold)
            if (canExpand) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded && canExpand,
            onDismissRequest = { expanded = false }
        ) {
            availableLanguages.forEach { languageCode ->
                DropdownMenuItem(
                    text = { Text(languageCode.uppercase()) },
                    onClick = {
                        expanded = false
                        onLanguageSelected(languageCode)
                    }
                )
            }
        }
    }
}

@Composable
private fun ColorLogicDropdown(
    selected: ColorMatchMode,
    onSelected: (ColorMatchMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(
        modifier = modifier,
        onClick = { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selected) {
                    ColorMatchMode.EXACTLY -> stringResource(id = R.string.search_color_exactly)
                    ColorMatchMode.AT_MOST -> stringResource(id = R.string.search_color_at_most)
                    ColorMatchMode.INCLUDING -> stringResource(id = R.string.search_color_including)
                }
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.search_color_exactly)) },
            onClick = {
                expanded = false
                onSelected(ColorMatchMode.EXACTLY)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.search_color_at_most)) },
            onClick = {
                expanded = false
                onSelected(ColorMatchMode.AT_MOST)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.search_color_including)) },
            onClick = {
                expanded = false
                onSelected(ColorMatchMode.INCLUDING)
            }
        )
    }
}

@Composable
private fun SortDropdown(
    selected: SearchSortBy,
    onSelected: (SearchSortBy) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(
        modifier = modifier,
        onClick = { expanded = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (selected) {
                    SearchSortBy.NAME -> stringResource(id = R.string.search_sort_name)
                    SearchSortBy.RARITY -> stringResource(id = R.string.search_sort_rarity)
                    SearchSortBy.CMC -> stringResource(id = R.string.search_sort_cmc)
                }
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.search_sort_name)) },
            onClick = {
                expanded = false
                onSelected(SearchSortBy.NAME)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.search_sort_rarity)) },
            onClick = {
                expanded = false
                onSelected(SearchSortBy.RARITY)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(id = R.string.search_sort_cmc)) },
            onClick = {
                expanded = false
                onSelected(SearchSortBy.CMC)
            }
        )
    }
}
