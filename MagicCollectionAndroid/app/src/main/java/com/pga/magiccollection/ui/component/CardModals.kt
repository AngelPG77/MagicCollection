package com.pga.magiccollection.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.pga.magiccollection.R
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.domain.model.enums.CardCondition
import com.pga.magiccollection.ui.designsystem.*

@Composable
fun VersionSelectionModal(
    versions: List<ScryfallCardDto>,
    isLoading: Boolean,
    onVersionSelected: (ScryfallCardDto) -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current
    val elevation = LocalAppElevation.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier.fillMaxWidth().padding(spacing.medium), 
            shape = shapes.extraLarge,
            tonalElevation = elevation.level3,
            shadowElevation = elevation.level4
        ) {
            Column(modifier = Modifier.padding(spacing.large), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.wantlist_select_version), 
                    style = MaterialTheme.typography.headlineSmall, 
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.tertiary, // Guild-specific title color
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(spacing.large))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(spacing.extraLarge), color = MaterialTheme.colorScheme.primary)
                } else {
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
                        items(versions) { version ->
                            Column(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(shapes.medium)
                                    .clickable { onVersionSelected(version) }
                                    .padding(spacing.small), 
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.aspectRatio(0.718f),
                                    shape = shapes.small,
                                    elevation = CardDefaults.cardElevation(defaultElevation = elevation.level2)
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(version.imageUris?.normal ?: version.imageUris?.small)
                                            .crossfade(true).build(),
                                        contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.height(spacing.smallPadding))
                                Text(
                                    text = version.setCode?.uppercase() ?: "", 
                                    style = MaterialTheme.typography.labelLarge, 
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = version.setName ?: "", 
                                    style = MaterialTheme.typography.bodySmall, 
                                    textAlign = TextAlign.Center, 
                                    maxLines = 2,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.large))
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) { 
                    Text(stringResource(id = R.string.action_cancel), fontWeight = FontWeight.Bold) 
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailEntryModal(
    version: ScryfallCardDto?,
    quantity: Int,
    foil: Boolean,
    condition: String,
    language: String,
    isSaving: Boolean,
    onQuantityChanged: (Int) -> Unit,
    onFoilChanged: (Boolean) -> Unit,
    onConditionChanged: (String) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    if (version == null) return
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(spacing.medium), shape = shapes.large) {
            Column(modifier = Modifier.padding(spacing.medium).verticalScroll(rememberScrollState())) {
                Text(text = stringResource(id = R.string.wantlist_card_details), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(spacing.medium))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(spacing.medium)) {
                    Card(modifier = Modifier.weight(0.4f).aspectRatio(0.718f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(version.imageUris?.normal ?: version.imageUris?.small)
                                .crossfade(true).build(), 
                            contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(spacing.smallPadding)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(id = R.string.wantlist_card_quantity_label))
                            IconButton(onClick = { if (quantity > 1) onQuantityChanged(quantity - 1) }) { Icon(Icons.Default.Remove, contentDescription = null) }
                            Text(quantity.toString(), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onQuantityChanged(quantity + 1) }) { Icon(Icons.Default.Add, contentDescription = null) }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = foil, onCheckedChange = onFoilChanged)
                            Text(stringResource(id = R.string.wantlist_card_foil))
                        }
                        
                        // Idioma
                        var langExp = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = langExp.value, onExpandedChange = { langExp.value = it }) {
                            OutlinedTextField(
                                value = language.uppercase(), 
                                onValueChange = {}, 
                                readOnly = true, 
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), 
                                label = { Text(stringResource(id = R.string.wantlist_card_language_label)) }, 
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExp.value) }
                            )
                            ExposedDropdownMenu(expanded = langExp.value, onDismissRequest = { langExp.value = false }) {
                                listOf("en", "es", "fr", "de", "it", "pt", "ja", "ko", "ru", "zhs", "zht").forEach { l ->
                                    DropdownMenuItem(text = { Text(l.uppercase()) }, onClick = { onLanguageChanged(l); langExp.value = false })
                                }
                            }
                        }

                        // Estado
                        var condExp = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = condExp.value, onExpandedChange = { condExp.value = it }) {
                            OutlinedTextField(
                                value = CardCondition.entries.find { it.name == condition }?.displayName ?: condition, 
                                onValueChange = {}, 
                                readOnly = true, 
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), 
                                label = { Text(stringResource(id = R.string.inventory_condition)) }, 
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExp.value) }
                            )
                            ExposedDropdownMenu(expanded = condExp.value, onDismissRequest = { condExp.value = false }) {
                                CardCondition.entries.forEach { c ->
                                    DropdownMenuItem(text = { Text(c.displayName) }, onClick = { onConditionChanged(c.name); condExp.value = false })
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.large))
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

/**
 * Unified edit modal for cards owned in a collection or on a want list.
 * The delete action lives here, inside the modal, keeping the card list item clean.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOwnedCardModal(
    imageUrl: String?,
    quantity: Int,
    foil: Boolean,
    condition: String,
    language: String,
    isSaving: Boolean,
    onQuantityChanged: (Int) -> Unit,
    onFoilChanged: (Boolean) -> Unit,
    onConditionChanged: (String) -> Unit,
    onLanguageChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val spacing = LocalAppSpacing.current
    val shapes = LocalAppShapes.current

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.medium),
            shape = shapes.large,
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(spacing.medium)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                // Header: title + delete button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.wantlist_card_details),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = spacing.smallPadding))

                Spacer(modifier = Modifier.height(spacing.smallPadding))

                // Image + controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(0.4f)
                            .aspectRatio(0.718f)
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier.weight(0.6f),
                        verticalArrangement = Arrangement.spacedBy(spacing.smallPadding)
                    ) {
                        // Quantity stepper
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(id = R.string.wantlist_card_quantity_label))
                            IconButton(onClick = { if (quantity > 1) onQuantityChanged(quantity - 1) }) {
                                Icon(Icons.Default.Remove, contentDescription = null)
                            }
                            Text(
                                text = quantity.toString(),
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { onQuantityChanged(quantity + 1) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                            }
                        }

                        // Foil toggle
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = foil, onCheckedChange = onFoilChanged)
                            Text(stringResource(id = R.string.wantlist_card_foil))
                        }

                        // Language
                        var langExp = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = langExp.value,
                            onExpandedChange = { langExp.value = it }
                        ) {
                            OutlinedTextField(
                                value = language.uppercase(),
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                label = { Text(stringResource(id = R.string.wantlist_card_language_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExp.value) }
                            )
                            ExposedDropdownMenu(
                                expanded = langExp.value,
                                onDismissRequest = { langExp.value = false }
                            ) {
                                listOf("en", "es", "fr", "de", "it", "pt", "ja", "ko", "ru", "zhs", "zht").forEach { l ->
                                    DropdownMenuItem(
                                        text = { Text(l.uppercase()) },
                                        onClick = { onLanguageChanged(l); langExp.value = false }
                                    )
                                }
                            }
                        }

                        // Condition
                        var condExp = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = condExp.value,
                            onExpandedChange = { condExp.value = it }
                        ) {
                            OutlinedTextField(
                                value = CardCondition.entries.find { it.name == condition }?.displayName ?: condition,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                label = { Text(stringResource(id = R.string.inventory_condition)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExp.value) }
                            )
                            ExposedDropdownMenu(
                                expanded = condExp.value,
                                onDismissRequest = { condExp.value = false }
                            ) {
                                CardCondition.entries.forEach { c ->
                                    DropdownMenuItem(
                                        text = { Text(c.displayName) },
                                        onClick = { onConditionChanged(c.name); condExp.value = false }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.large))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(id = R.string.action_cancel))
                    }
                    Spacer(modifier = Modifier.width(spacing.smallPadding))
                    Button(onClick = onSave, enabled = !isSaving) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            Text(stringResource(id = R.string.action_save))
                        }
                    }
                }
            }
        }
    }
}
