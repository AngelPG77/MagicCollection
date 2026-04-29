package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pga.magiccollection.R
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.domain.model.enums.CardCondition
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.SearchSortBy
import com.pga.magiccollection.ui.component.CardGrid
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WantListAddCardScreen(
    viewModel: WantListAddCardViewModel,
    wantListLocalId: Long,
    gridSize: Int,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isSuggestionMode = !uiState.isSearchPerformed && uiState.query.isNotBlank()

    // Estados locales para el input "en curso" antes del onBlur
    var localQuery by remember(uiState.query) { mutableStateOf(uiState.query) }
    var localType by remember(uiState.type) { mutableStateOf(uiState.type) }
    var isSetFieldFocused by remember { mutableStateOf(false) }

    // Definición de colores de Magic
    val mtgColors = mapOf(
        "W" to Color(0xFFFFFBD5),
        "U" to Color(0xFFAAE0FA),
        "B" to Color(0xFF121212),
        "R" to Color(0xFFF9AA8F),
        "G" to Color(0xFF9BD3AE),
        "C" to Color(0xFFCCCCCC)
    )

    val hasActiveFilters = uiState.selectedColors.isNotEmpty() ||
            uiState.type.isNotBlank() ||
            uiState.selectedRarities.isNotEmpty() ||
            uiState.selectedSetCode != null ||
            uiState.useColorIdentity ||
            uiState.colorLogic != ColorMatchMode.EXACTLY ||
            uiState.sortBy != SearchSortBy.NAME ||
            !uiState.sortAscending

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
                    Icon(Icons.Default.Search, contentDescription = null)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = padding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    bottom = padding.calculateBottomPadding(),
                    top = 8.dp
                )
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Cabecera con buscador (Igual que SearchScreen)
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
                                    .onFocusChanged { state ->
                                        if (!state.isFocused) {
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
                                            Icon(Icons.Default.Close, contentDescription = null)
                                        }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    autoCorrectEnabled = false,
                                    keyboardType = KeyboardType.Password,
                                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                                ),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onSearch = {
                                        viewModel.onQueryChanged(localQuery)
                                        viewModel.performSearch()
                                    }
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
                                    AssistChip(onClick = {}, label = { Text(label) })
                                }
                            }
                            if (hasActiveFilters) {
                                IconButton(onClick = { viewModel.clearFilters() }) {
                                    Icon(Icons.Default.FilterAltOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }

            // Cuerpo: Filtros o Resultados
            Box(modifier = Modifier.fillMaxSize()) {
                if (isSuggestionMode || uiState.isSearchPerformed) {
                    // Mostrar Resultados con Ordenación en la parte superior
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
                                    contentDescription = null
                                )
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when {
                                uiState.errorMessage != null -> {
                                    Text(uiState.errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.align(Alignment.Center))
                                }
                                uiState.searchResults.isEmpty() && !uiState.isLoading -> {
                                    val message = if (uiState.hasIndexData) {
                                        stringResource(id = R.string.search_no_results)
                                    } else {
                                        stringResource(id = R.string.index_preparing)
                                    }
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        if (!uiState.hasIndexData) {
                                            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
                                        }
                                        Text(
                                            text = message,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(horizontal = 32.dp)
                                        )
                                    }
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
                    // Mostrar Filtros
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
                                val baseColor = mtgColors[color] ?: Color.Gray
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.onColorToggled(color) },
                                    label = { Text(color, color = if (color == "B") Color.White else Color.Unspecified) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = baseColor.copy(alpha = 0.6f),
                                        selectedContainerColor = baseColor.copy(alpha = 1.0f)
                                    )
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
                                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done),
                                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                                    onDone = { 
                                        viewModel.onTypeConfirmed(localType)
                                    }
                                )
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
                                                    viewModel.onTypeConfirmed(type)
                                                    isTypeFieldFocused = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Edición
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = stringResource(id = R.string.search_filter_edition), 
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            OutlinedTextField(
                                value = if (uiState.selectedSetCode != null && uiState.setQuery.isBlank()) "" else uiState.setQuery,
                                onValueChange = viewModel::onSetQueryChanged,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onFocusChanged { state -> isSetFieldFocused = state.isFocused },
                                placeholder = { Text(uiState.selectedSetName ?: stringResource(id = R.string.search_filter_edition_hint_placeholder)) },
                                trailingIcon = {
                                    if (uiState.selectedSetCode != null) {
                                        IconButton(onClick = viewModel::onClearSet) { Icon(Icons.Default.Close, contentDescription = null) }
                                    }
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    autoCorrectEnabled = false,
                                    keyboardType = KeyboardType.Password
                                )
                            )
                            if (isSetFieldFocused || uiState.setQuery.isNotBlank()) {
                                Card(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)) {
                                    LazyColumn {
                                        items(uiState.filteredSets) { mtgSet ->
                                            ListItem(
                                                headlineContent = { Text(mtgSet.name) },
                                                supportingContent = { Text(mtgSet.code.uppercase()) },
                                                modifier = Modifier.clickable { viewModel.onSetSelected(mtgSet) }
                                            )
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

        // Modals
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

// Copiar componentes auxiliares de SearchScreen.kt para mantener consistencia exacta
@Composable
private fun LanguageDropdown(activeLanguage: String, availableLanguages: List<String>, onLanguageSelected: (String) -> Unit) {
    val canExpand = availableLanguages.size > 1
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { if (canExpand) expanded = true }) {
            Text(activeLanguage.uppercase(), fontWeight = FontWeight.Bold)
            if (canExpand) Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded && canExpand, onDismissRequest = { expanded = false }) {
            availableLanguages.forEach { code ->
                DropdownMenuItem(text = { Text(code.uppercase()) }, onClick = { expanded = false; onLanguageSelected(code) })
            }
        }
    }
}

@Composable
private fun ColorLogicDropdown(selected: ColorMatchMode, onSelected: (ColorMatchMode) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(modifier = modifier, onClick = { expanded = true }) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = when (selected) {
                ColorMatchMode.EXACTLY -> stringResource(id = R.string.search_color_exactly)
                ColorMatchMode.AT_MOST -> stringResource(id = R.string.search_color_at_most)
                ColorMatchMode.INCLUDING -> stringResource(id = R.string.search_color_including)
            })
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf(ColorMatchMode.EXACTLY, ColorMatchMode.AT_MOST, ColorMatchMode.INCLUDING).forEach { mode ->
            DropdownMenuItem(text = { Text(stringResource(id = when(mode) {
                ColorMatchMode.EXACTLY -> R.string.search_color_exactly
                ColorMatchMode.AT_MOST -> R.string.search_color_at_most
                ColorMatchMode.INCLUDING -> R.string.search_color_including
            })) }, onClick = { expanded = false; onSelected(mode) })
        }
    }
}

@Composable
private fun SortDropdown(selected: SearchSortBy, onSelected: (SearchSortBy) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    OutlinedCard(modifier = modifier, onClick = { expanded = true }) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(text = when (selected) {
                SearchSortBy.NAME -> stringResource(id = R.string.search_sort_name)
                SearchSortBy.RARITY -> stringResource(id = R.string.search_sort_rarity)
                SearchSortBy.CMC -> stringResource(id = R.string.search_sort_cmc)
            })
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf(SearchSortBy.NAME, SearchSortBy.RARITY, SearchSortBy.CMC).forEach { sort ->
            DropdownMenuItem(text = { Text(stringResource(id = when(sort) {
                SearchSortBy.NAME -> R.string.search_sort_name
                SearchSortBy.RARITY -> R.string.search_sort_rarity
                SearchSortBy.CMC -> R.string.search_sort_cmc
            })) }, onClick = { expanded = false; onSelected(sort) })
        }
    }
}

// Modales específicos para WantList
@Composable
fun VersionSelectionModal(versions: List<ScryfallCardDto>, isLoading: Boolean, onVersionSelected: (ScryfallCardDto) -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = stringResource(id = R.string.wantlist_select_version), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                } else {
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(versions) { version ->
                            Column(modifier = Modifier.width(150.dp).clickable { onVersionSelected(version) }, horizontalAlignment = Alignment.CenterHorizontally) {
                                Card(modifier = Modifier.aspectRatio(0.718f)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current).data(version.imageUris?.normal ?: version.imageUris?.small).crossfade(true).build(),
                                        contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = version.setCode?.uppercase() ?: "", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Text(text = version.setName ?: "", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 2)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.action_cancel)) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailEntryModal(version: ScryfallCardDto?, quantity: Int, foil: Boolean, condition: String, language: String, isSaving: Boolean, onQuantityChanged: (Int) -> Unit, onFoilChanged: (Boolean) -> Unit, onConditionChanged: (String) -> Unit, onLanguageChanged: (String) -> Unit, onSave: () -> Unit, onDismiss: () -> Unit) {
    if (version == null) return
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = stringResource(id = R.string.wantlist_card_details), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.weight(0.4f).aspectRatio(0.718f)) {
                        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(version.imageUris?.normal ?: version.imageUris?.small).crossfade(true).build(), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(id = R.string.wantlist_card_quantity_label))
                            IconButton(onClick = { if (quantity > 1) onQuantityChanged(quantity - 1) }) { Icon(Icons.Default.Remove, contentDescription = null) }
                            Text(quantity.toString(), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onQuantityChanged(quantity + 1) }) { Icon(Icons.Default.Add, contentDescription = null) }
                        }
                        if (version.foil == true && version.nonfoil == true) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = foil, onCheckedChange = onFoilChanged)
                                Text(stringResource(id = R.string.wantlist_card_foil))
                            }
                        } else if (version.foil == true) {
                            Text(stringResource(id = R.string.wantlist_card_foil_only), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        // Idioma
                        var langExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = langExp, onExpandedChange = { langExp = it }) {
                            OutlinedTextField(value = language.uppercase(), onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), label = { Text(stringResource(id = R.string.wantlist_card_language_label)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExp) })
                            ExposedDropdownMenu(expanded = langExp, onDismissRequest = { langExp = false }) {
                                listOf("en", "es", "fr", "de", "it", "pt", "ja", "ko", "ru", "zhs", "zht").forEach { l ->
                                    DropdownMenuItem(text = { Text(l.uppercase()) }, onClick = { onLanguageChanged(l); langExp = false })
                                }
                            }
                        }

                        // Estado
                        var condExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = condExp, onExpandedChange = { condExp = it }) {
                            OutlinedTextField(value = CardCondition.entries.find { it.name == condition }?.displayName ?: condition, onValueChange = {}, readOnly = true, modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), label = { Text(stringResource(id = R.string.inventory_condition)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExp) })
                            ExposedDropdownMenu(expanded = condExp, onDismissRequest = { condExp = false }) {
                                CardCondition.entries.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.displayName) }, onClick = { onConditionChanged(c.name); condExp = false })
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(id = R.string.action_cancel)) }
                    Button(onClick = onSave, enabled = !isSaving) {
                        if (isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp)) else Text(stringResource(id = R.string.action_save))
                    }
                }
            }
        }
    }
}
