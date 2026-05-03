package com.pga.magiccollection.ui.component

import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColorLogicDropdown(
    selected: ColorMatchMode,
    onSelected: (ColorMatchMode) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedCard(onClick = { expanded = true }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                DropdownMenuItem(
                    text = {
                        Text(stringResource(id = when(mode) {
                            ColorMatchMode.EXACTLY -> R.string.search_color_exactly
                            ColorMatchMode.AT_MOST -> R.string.search_color_at_most
                            ColorMatchMode.INCLUDING -> R.string.search_color_including
                        }))
                    },
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
    OutlinedCard(modifier = modifier, onClick = { expanded = true }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            DropdownMenuItem(
                text = {
                    Text(stringResource(id = when(sort) {
                        SearchSortBy.NAME -> R.string.search_sort_name
                        SearchSortBy.RARITY -> R.string.search_sort_rarity
                        SearchSortBy.CMC -> R.string.search_sort_cmc
                    }))
                },
                onClick = {
                    expanded = false
                    onSelected(sort)
                }
            )
        }
    }
}
