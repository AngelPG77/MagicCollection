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
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@Composable
fun VersionSelectionModal(
    versions: List<ScryfallCardDto>,
    isLoading: Boolean,
    onVersionSelected: (ScryfallCardDto) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.wantlist_select_version), 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                } else {
                    LazyRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(versions) { version ->
                            Column(
                                modifier = Modifier.width(150.dp).clickable { onVersionSelected(version) }, 
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(modifier = Modifier.aspectRatio(0.718f)) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(version.imageUris?.normal ?: version.imageUris?.small)
                                            .crossfade(true).build(),
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
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(text = stringResource(id = R.string.wantlist_card_details), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.weight(0.4f).aspectRatio(0.718f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(version.imageUris?.normal ?: version.imageUris?.small)
                                .crossfade(true).build(), 
                            contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(0.6f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
