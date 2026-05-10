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

import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip

@Composable
fun ManaColorFilter(
    color: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val resId = when (color) {
        "W" -> R.drawable.ic_mana_w
        "U" -> R.drawable.ic_mana_u
        "B" -> R.drawable.ic_mana_b
        "R" -> R.drawable.ic_mana_r
        "G" -> R.drawable.ic_mana_g
        "C" -> R.drawable.ic_mana_c
        else -> null
    }

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        if (resId != null) {
            Image(
                painter = painterResource(id = resId),
                contentDescription = color,
                modifier = Modifier
                    .size(34.dp)
                    .alpha(if (isSelected) 1f else 0.4f)
                    .then(
                        if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        else Modifier
                    )
            )
        } else {
            Surface(
                modifier = Modifier.size(34.dp),
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = color,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

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
                    text = when (selected) {
                        ColorMatchMode.EXACTLY -> stringResource(id = R.string.search_color_exactly)
                        ColorMatchMode.AT_MOST -> stringResource(id = R.string.search_color_at_most)
                        ColorMatchMode.INCLUDING -> stringResource(id = R.string.search_color_including)
                    },
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
