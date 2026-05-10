package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.unit.LayoutDirection
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.entities.WantListCardEntity
import com.pga.magiccollection.domain.model.enums.CardCondition
import com.pga.magiccollection.ui.component.EmptyState
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WantListDetailScreen(
    viewModel: WantListViewModel,
    mainViewModel: MainViewModel,
    wantListLocalId: Long,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToAddCard: () -> Unit,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(wantListLocalId) {
        viewModel.loadWantListCards(wantListLocalId)
    }

    // Actualizar el título de la App Bar general con el nombre de la lista
    val listName = uiState.selectedWantList?.name
    LaunchedEffect(listName) {
        if (listName != null) {
            mainViewModel.setTopBarTitle(listName)
        }
    }

    // Limpiar el título al salir
    DisposableEffect(Unit) {
        onDispose {
            mainViewModel.setTopBarTitle(null)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
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

    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        snackbarHost = { MagicCollectionSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddCard,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(id = R.string.wantlist_add_card))
            }
        }
    ) { padding ->
        val innerPadding = PaddingValues(
            start = padding.calculateStartPadding(LayoutDirection.Ltr),
            end = padding.calculateEndPadding(LayoutDirection.Ltr),
            bottom = padding.calculateBottomPadding()
        )
        
        if (uiState.selectedWantListCards.isEmpty()) {
            EmptyState(
                title = stringResource(id = R.string.wantlist_detail_empty),
                icon = Icons.Default.Add,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.selectedWantListCards, key = { it.localId }) { card ->
                    Box(modifier = Modifier.animateItem()) {
                        WantListCardItem(
                            card = card,
                            onClick = { onNavigateToDetail(card.scryfallId) },
                            onEdit = { viewModel.showEditCardModal(card) },
                            onRemove = { viewModel.removeCard(card.localId) }
                        )
                    }
                }
            }
        }
    }

    if (uiState.showEditCardModal) {
        EditCardEntryModal(
            card = uiState.editingCard,
            quantity = uiState.editQuantity,
            foil = uiState.editFoil,
            condition = uiState.editCondition,
            language = uiState.editLanguage,
            isSaving = uiState.isSavingCard,
            onQuantityChanged = viewModel::onEditQuantityChanged,
            onFoilChanged = viewModel::onEditFoilChanged,
            onConditionChanged = viewModel::onEditConditionChanged,
            onLanguageChanged = viewModel::onEditLanguageChanged,
            onSave = viewModel::saveEditedCard,
            onDismiss = { viewModel.showEditCardModal(null) }
        )
    }
}

@Composable
fun WantListCardItem(
    card: WantListCardEntity,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Card Image
            Card(
                modifier = Modifier
                    .width(80.dp)
                    .aspectRatio(0.718f),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(card.imageUrl)
                        .size(160, 223)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build(),
                    contentDescription = card.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Card Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Type Line
                if (!card.typeLine.isNullOrBlank()) {
                    Text(
                        text = card.typeLine,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Mana Cost
                if (!card.manaCost.isNullOrBlank()) {
                    Text(
                        text = card.manaCost,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Foil and Language badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (card.foil) {
                        SuggestionChip(
                            onClick = {},
                            label = { 
                                Text(
                                    text = stringResource(id = R.string.wantlist_card_foil),
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            }
                        )
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { 
                            Text(
                                text = card.language.uppercase(),
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        }
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { 
                            Text(
                                text = card.condition.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall
                            ) 
                        }
                    )
                }
            }
            
            // Quantity and Actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(id = R.string.wantlist_card_quantity, card.quantity),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.action_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardEntryModal(
    card: WantListCardEntity?,
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
    if (card == null) return
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ) {
            Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(id = R.string.wantlist_card_details),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Card(modifier = Modifier.weight(0.4f).aspectRatio(0.718f)) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(card.imageUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
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
                        var langExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = langExp, onExpandedChange = { langExp = it }) {
                            OutlinedTextField(
                                value = language.uppercase(),
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                label = { Text(stringResource(id = R.string.wantlist_card_language_label)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExp) }
                            )
                            ExposedDropdownMenu(expanded = langExp, onDismissRequest = { langExp = false }) {
                                listOf("en", "es", "fr", "de", "it", "pt", "ja", "ko", "ru", "zhs", "zht").forEach { l ->
                                    DropdownMenuItem(text = { Text(l.uppercase()) }, onClick = { onLanguageChanged(l); langExp = false })
                                }
                            }
                        }

                        // Estado
                        var condExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = condExp, onExpandedChange = { condExp = it }) {
                            OutlinedTextField(
                                value = CardCondition.entries.find { it.name == condition }?.displayName ?: condition,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                                label = { Text(stringResource(id = R.string.inventory_condition)) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExp) }
                            )
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
