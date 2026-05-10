package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.unit.LayoutDirection
import com.pga.magiccollection.R
import com.pga.magiccollection.data.local.entities.WantListEntity
import com.pga.magiccollection.ui.component.EmptyState
import com.pga.magiccollection.ui.component.MagicCollectionSnackbarHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WantListScreen(
    viewModel: WantListViewModel,
    mainViewModel: MainViewModel,
    isLoggedIn: Boolean,
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Asegurar que el título de la App Bar sea el por defecto
    LaunchedEffect(Unit) {
        mainViewModel.setTopBarTitle(null)
    }

    // Manejar sesión expirada
    LaunchedEffect(uiState.isSessionExpired) {
        if (uiState.isSessionExpired) {
            onNavigateToLogin()
        }
    }

    // Create Dialog
    if (uiState.showCreateDialog) {
        CreateEditWantListDialog(
            title = stringResource(id = R.string.wantlist_create_title),
            nameValue = uiState.nameInput,
            onNameChange = { viewModel.onNameInputChanged(it) },
            onConfirm = { viewModel.createWantList() },
            onDismiss = { viewModel.showCreateDialog(false) },
            isLoading = uiState.isLoading,
            errorMessage = uiState.nameError,
            showDeleteButton = false,
            onDelete = {}
        )
    }

    // Edit Dialog
    if (uiState.showEditDialog) {
        CreateEditWantListDialog(
            title = stringResource(id = R.string.wantlist_edit_title),
            nameValue = uiState.nameInput,
            onNameChange = { viewModel.onNameInputChanged(it) },
            onConfirm = { viewModel.updateWantList() },
            onDismiss = { viewModel.showEditDialog(null) },
            isLoading = uiState.isLoading,
            errorMessage = uiState.nameError,
            showDeleteButton = true,
            onDelete = { viewModel.deleteWantList() }
        )
    }

    // Snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
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
            if (isLoggedIn) {
                FloatingActionButton(
                    onClick = { viewModel.showCreateDialog(true) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
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
        ) {
            if (isLoggedIn) {
                // Search Bar
                Surface(
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 8.dp, top = 0.dp),
                        placeholder = { Text(stringResource(id = R.string.wantlist_name_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(Icons.Default.Close, contentDescription = null)
                                }
                            }
                        },
                        singleLine = true
                    )
                }

                if (uiState.wantLists.isEmpty()) {
                    EmptyState(
                        title = stringResource(id = R.string.wantlist_empty),
                        icon = Icons.Default.Favorite,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (uiState.filteredWantLists.isEmpty()) {
                    EmptyState(
                        title = stringResource(id = R.string.search_no_results),
                        icon = Icons.Default.SearchOff,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Filtered WantLists
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.filteredWantLists, key = { it.localId }) { wantList ->
                            Box(modifier = Modifier.animateItem()) {
                                WantListItem(
                                    name = wantList.name,
                                    cardCount = wantList.cardCount,
                                    onClick = { onNavigateToDetail(wantList.localId) },
                                    onEditClick = { viewModel.showEditDialog(
                                        WantListEntity(
                                            localId = wantList.localId,
                                            remoteId = wantList.remoteId,
                                            name = wantList.name,
                                            userId = wantList.userId,
                                            synced = wantList.synced
                                        )
                                    ) }
                                )
                            }
                        }
                    }
                }
            } else {
                // Login required message
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(id = R.string.wantlist_login_required),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onNavigateToLogin) {
                            Text(stringResource(id = R.string.login_now))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WantListItem(
    name: String,
    cardCount: Int? = null,
    onClick: () -> Unit,
    onEditClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.AutoMirrored.Filled.List,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (cardCount != null) {
                    Text(
                        text = stringResource(id = R.string.collection_card_count, cardCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (onEditClick != null) {
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
fun CreateEditWantListDialog(
    title: String,
    nameValue: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    showDeleteButton: Boolean,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, modifier = Modifier.weight(1f))
                if (showDeleteButton) {
                    IconButton(
                        onClick = onDelete,
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nameValue,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(id = R.string.wantlist_name_hint)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    val displayError = if (errorMessage.contains("wantlist_")) {
                        val resId = context.resources.getIdentifier(errorMessage, "string", context.packageName)
                        if (resId != 0) context.getString(resId) else errorMessage
                    } else errorMessage
                    Text(
                        text = displayError,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading && nameValue.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(id = R.string.action_confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}
