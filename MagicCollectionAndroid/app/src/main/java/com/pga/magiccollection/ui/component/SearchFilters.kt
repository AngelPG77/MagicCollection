package com.pga.magiccollection.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R
import com.pga.magiccollection.domain.model.search.ColorMatchMode
import com.pga.magiccollection.domain.model.search.SearchSortBy

@Composable
fun LanguageDropdown(
    activeLanguage: String,
    availableLanguages: List<String>,
    onLanguageSelected: (String) -> Unit
) {
    val canExpand = availableLanguages.size > 1
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { if (canExpand) expanded = true }) {
            Text(activeLanguage.uppercase(), fontWeight = FontWeight.Bold)
            if (canExpand) Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded && canExpand, onDismissRequest = { expanded = false }) {
            availableLanguages.forEach { code ->
                DropdownMenuItem(
                    text = { Text(code.uppercase()) },
                    onClick = {
                        expanded = false
                        onLanguageSelected(code)
                    }
                )
            }
        }
    }
}

@Composable
fun ColorLogicDropdown(
    selected: ColorMatchMode,
    onSelected: (ColorMatchMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Resolve labels in the parent composition. DropdownMenu renders inside a Popup
    // whose composition does NOT inherit the activity-level LocalContext override
    // that carries the user's preferred-language ContextWrapper, so a stringResource()
    // call placed inside the popup would fall back to the system locale.
    val exactlyLabel = stringResource(id = R.string.search_color_exactly)
    val atMostLabel = stringResource(id = R.string.search_color_at_most)
    val includingLabel = stringResource(id = R.string.search_color_including)
    val labelFor: (ColorMatchMode) -> String = { mode ->
        when (mode) {
            ColorMatchMode.EXACTLY -> exactlyLabel
            ColorMatchMode.AT_MOST -> atMostLabel
            ColorMatchMode.INCLUDING -> includingLabel
        }
    }

    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = labelFor(selected),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = androidx.compose.ui.Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            listOf(ColorMatchMode.EXACTLY, ColorMatchMode.AT_MOST, ColorMatchMode.INCLUDING).forEach { mode ->
                DropdownMenuItem(
                    text = { Text(labelFor(mode)) },
                    onClick = {
                        expanded = false
                        onSelected(mode)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortDropdown(
    selected: SearchSortBy,
    onSelected: (SearchSortBy) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    // Resolve labels in the parent composition — see comment in ColorLogicDropdown.
    val nameLabel = stringResource(id = R.string.search_sort_name)
    val rarityLabel = stringResource(id = R.string.search_sort_rarity)
    val cmcLabel = stringResource(id = R.string.search_sort_cmc)
    val labelFor: (SearchSortBy) -> String = { sort ->
        when (sort) {
            SearchSortBy.NAME -> nameLabel
            SearchSortBy.RARITY -> rarityLabel
            SearchSortBy.CMC -> cmcLabel
        }
    }

    OutlinedCard(modifier = modifier, onClick = { expanded = true }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = labelFor(selected))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
        }
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        listOf(SearchSortBy.NAME, SearchSortBy.RARITY, SearchSortBy.CMC).forEach { sort ->
            DropdownMenuItem(
                text = { Text(labelFor(sort)) },
                onClick = {
                    expanded = false
                    onSelected(sort)
                }
            )
        }
    }
}
