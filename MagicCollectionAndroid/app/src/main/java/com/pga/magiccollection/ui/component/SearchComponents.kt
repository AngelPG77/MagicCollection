package com.pga.magiccollection.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAltOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.entities.MtgSetEntity
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.ui.designsystem.*

@Composable
fun SearchHeader(
    query: String,
    onQueryChange: (String) -> Unit,
    onQueryConfirmed: (String) -> Unit,
    onClearQuery: () -> Unit,
    onSearch: () -> Unit,
    activeLanguage: String,
    availableLanguages: List<String>,
    onLanguageSelected: (String) -> Unit,
    filterLabels: List<String>,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val elevation = LocalAppElevation.current

    Surface(
        tonalElevation = elevation.level2,
        shadowElevation = elevation.level2,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.smallPadding, vertical = spacing.smallPadding),
            verticalArrangement = Arrangement.spacedBy(spacing.small)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(spacing.extraSmall)) {
                Text(
                    text = stringResource(id = R.string.search_filter_name),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = spacing.extraSmall)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.small)
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { state ->
                                if (!state.isFocused) {
                                    onQueryConfirmed(query)
                                }
                            },
                        placeholder = { Text(stringResource(id = R.string.search_name_hint_placeholder)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = onClearQuery) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            autoCorrectEnabled = false,
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Search
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = { onSearch() }
                        ),
                        shape = MaterialTheme.shapes.medium
                    )

                    LanguageDropdown(
                        activeLanguage = activeLanguage,
                        availableLanguages = availableLanguages,
                        onLanguageSelected = onLanguageSelected
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
                        horizontalArrangement = Arrangement.spacedBy(spacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(filterLabels) { label ->
                            AssistChip(onClick = {}, label = { Text(label) })
                        }
                    }
                    if (hasActiveFilters) {
                        IconButton(onClick = onClearFilters) {
                            Icon(Icons.Default.FilterAltOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterSection(
    selectedColors: Set<String>,
    onColorToggled: (String) -> Unit,
    useColorIdentity: Boolean,
    onUseColorIdentityChange: (Boolean) -> Unit,
    colorLogic: ColorMatchMode,
    onColorLogicChange: (ColorMatchMode) -> Unit,
    typeQuery: String,
    onTypeQueryChange: (String) -> Unit,
    onTypeConfirmed: (String) -> Unit,
    filteredTypes: List<String>,
    setQuery: String,
    onSetQueryChange: (String) -> Unit,
    onSetSelected: (MtgSetEntity) -> Unit,
    onClearSet: () -> Unit,
    selectedSetName: String?,
    filteredSets: List<MtgSetEntity>,
    selectedRarities: Set<String>,
    onRarityToggled: (String) -> Unit,
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalAppSpacing.current
    val elevation = LocalAppElevation.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.medium),
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        Text(stringResource(id = R.string.search_filter_colors), style = MaterialTheme.typography.titleSmall)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("W", "U", "B", "R", "G", "C").forEach { color ->
                ManaColorFilter(
                    color = color,
                    isSelected = color in selectedColors,
                    onClick = { onColorToggled(color) }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small), verticalAlignment = Alignment.CenterVertically) {
            FilterChip(
                selected = useColorIdentity,
                onClick = { onUseColorIdentityChange(!useColorIdentity) },
                label = { Text(stringResource(id = R.string.search_filter_color_identity)) }
            )
            ColorLogicDropdown(selected = colorLogic, onSelected = onColorLogicChange, modifier = Modifier.weight(1f))
        }

        var isTypeFieldFocused by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Text(
                text = stringResource(id = R.string.search_filter_type),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = typeQuery,
                onValueChange = onTypeQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state ->
                        isTypeFieldFocused = state.isFocused
                        if (!state.isFocused) {
                            onTypeConfirmed(typeQuery)
                        }
                    },
                placeholder = { Text(stringResource(id = R.string.search_type_hint_placeholder)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onTypeConfirmed(typeQuery) }),
                shape = MaterialTheme.shapes.medium
            )
            
            if (isTypeFieldFocused || typeQuery.isNotBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation.level2)
                ) {
                    LazyColumn {
                        items(filteredTypes) { type ->
                            ListItem(
                                headlineContent = { Text(type) },
                                modifier = Modifier.clickable { 
                                    onTypeConfirmed(type)
                                    isTypeFieldFocused = false
                                }
                            )
                        }
                    }
                }
            }
        }

        var isSetFieldFocused by remember { mutableStateOf(false) }
        Column(verticalArrangement = Arrangement.spacedBy(spacing.small)) {
            Text(
                text = stringResource(id = R.string.search_filter_edition), 
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            OutlinedTextField(
                value = setQuery,
                onValueChange = onSetQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { state -> isSetFieldFocused = state.isFocused },
                placeholder = { Text(selectedSetName ?: stringResource(id = R.string.search_filter_edition_hint_placeholder)) },
                trailingIcon = {
                    if (selectedSetName != null) {
                        IconButton(onClick = onClearSet) { Icon(Icons.Default.Close, contentDescription = null) }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, keyboardType = KeyboardType.Text),
                shape = MaterialTheme.shapes.medium
            )
            if (isSetFieldFocused || setQuery.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp)) {
                    LazyColumn {
                        items(filteredSets) { mtgSet ->
                            ListItem(
                                headlineContent = { Text(mtgSet.name) },
                                supportingContent = { Text(mtgSet.code.uppercase()) },
                                modifier = Modifier.clickable { 
                                    onSetSelected(mtgSet)
                                    isSetFieldFocused = false
                                }
                            )
                        }
                    }
                }
            }
        }

        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.small)) {
            listOf("common" to R.string.search_rarity_common, "uncommon" to R.string.search_rarity_uncommon, "rare" to R.string.search_rarity_rare, "mythic" to R.string.search_rarity_mythic).forEach { (rarity, label) ->
                FilterChip(selected = rarity in selectedRarities, onClick = { onRarityToggled(rarity) }, label = { Text(stringResource(id = label)) })
            }
        }
        
        if (hasActiveFilters) {
            TextButton(onClick = onClearFilters, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(id = R.string.search_clear_filters))
            }
        }
    }
}
