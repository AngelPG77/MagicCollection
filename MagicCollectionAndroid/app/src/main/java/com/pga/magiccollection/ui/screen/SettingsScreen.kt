package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R
import com.pga.magiccollection.ui.theme.ThemeColors

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToGuides: () -> Unit,
    onNavigateToContact: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState(initial = null)

    // Efecto para guardar al salir si fuera necesario (aunque ya se guarda en cada cambio)
    DisposableEffect(Unit) {
        onDispose {
            // Aquí podríamos forzar un guardado final si tuviéramos estados temporales
        }
    }

    // Diálogos de Gestión de Cuenta
    if (uiState.showUpdateUsernameDialog) {
        UpdateUsernameDialog(
            currentValue = uiState.newUsernameInput,
            onValueChange = { viewModel.onNewUsernameChanged(it) },
            onConfirm = { viewModel.updateUsername() },
            onDismiss = { viewModel.showUpdateUsernameDialog(false) },
            isLoading = uiState.authLoading,
            errorMessage = uiState.authMessage
        )
    }

    if (uiState.showChangePasswordDialog) {
        ChangePasswordDialog(
            currentPassword = uiState.currentPasswordInput,
            password = uiState.newPasswordInput,
            repeatPassword = uiState.repeatPasswordInput,
            onCurrentPasswordChange = { viewModel.onCurrentPasswordChanged(it) },
            onPasswordChange = { viewModel.onNewPasswordChanged(it) },
            onRepeatPasswordChange = { viewModel.onRepeatPasswordChanged(it) },
            isCurrentPasswordVisible = uiState.isCurrentPasswordVisible,
            isNewPasswordVisible = uiState.isPasswordVisible,
            isRepeatPasswordVisible = uiState.isRepeatPasswordVisible,
            onToggleCurrentVisibility = { viewModel.toggleCurrentPasswordVisibility() },
            onToggleNewVisibility = { viewModel.togglePasswordVisibility() },
            onToggleRepeatVisibility = { viewModel.toggleRepeatPasswordVisibility() },
            onConfirm = { viewModel.updatePassword() },
            onDismiss = { viewModel.showChangePasswordDialog(false) },
            isLoading = uiState.authLoading,
            errorMessage = uiState.authMessage
        )
    }

    if (uiState.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.showDeleteAccountDialog(false) },
            title = { Text(stringResource(id = R.string.dialog_delete_account_title)) },
            text = { Text(stringResource(id = R.string.dialog_delete_account_message)) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteUser() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    if (uiState.authLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    } else {
                        Text(stringResource(id = R.string.action_delete))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showDeleteAccountDialog(false) }) {
                    Text(stringResource(id = R.string.action_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card de Usuario
        UserHeaderCard(
            isLoggedIn = uiState.isLoggedIn,
            username = uiState.currentUsername,
            onRegisterClick = onNavigateToRegister,
            onLoginClick = onNavigateToLogin,
            onLogoutClick = { viewModel.logout() }
        )

        if (preferences != null) {
            // Sección Cuenta (Solo si está logueado)
            if (uiState.isLoggedIn) {
                SettingsSection(title = stringResource(id = R.string.settings_section_account)) {
                    TextSettingsItem(
                        title = stringResource(id = R.string.settings_update_username),
                        icon = Icons.Default.Edit,
                        onClick = { viewModel.showUpdateUsernameDialog(true) }
                    )
                    TextSettingsItem(
                        title = stringResource(id = R.string.settings_change_password),
                        icon = Icons.Default.Lock,
                        onClick = { viewModel.showChangePasswordDialog(true) }
                    )
                    TextSettingsItem(
                        title = stringResource(id = R.string.settings_delete_account),
                        icon = Icons.Default.Delete,
                        onClick = { viewModel.showDeleteAccountDialog(true) },
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Sección General
            SettingsSection(title = stringResource(id = R.string.settings_section_general)) {
                SyncStatusItem(
                    isSynced = uiState.collections.all { it.synced },
                    onSyncClick = { viewModel.syncCollections() }
                )
            }

            // Sección Idioma
            SettingsSection(title = stringResource(id = R.string.settings_section_language)) {
                TextSettingsItem(
                    title = stringResource(id = R.string.settings_search_language),
                    subtitle = if (preferences!!.searchLanguage == "en") stringResource(id = R.string.lang_en) else stringResource(id = R.string.lang_es),
                    icon = Icons.Default.Search,
                    onClick = { 
                        val newLang = if (preferences!!.searchLanguage == "en") "es" else "en"
                        viewModel.updateSearchLanguage(newLang)
                    }
                )
                TextSettingsItem(
                    title = stringResource(id = R.string.settings_app_language),
                    subtitle = if (preferences!!.appLanguage == "en") stringResource(id = R.string.lang_en) else stringResource(id = R.string.lang_es),
                    icon = Icons.Default.Info,
                    onClick = { 
                        val newLang = if (preferences!!.appLanguage == "en") "es" else "en"
                        viewModel.updateAppLanguage(newLang)
                    }
                )
            }

            // Sección Apariencia
            SettingsSection(title = stringResource(id = R.string.settings_section_appearance)) {
                ToggleSettingsItem(
                    title = stringResource(id = R.string.settings_dark_theme),
                    checked = preferences!!.darkTheme,
                    onCheckedChange = { viewModel.updateDarkTheme(it) },
                    icon = Icons.Default.Build
                )
                ColorSettingsItem(
                    selectedColor = preferences!!.themeColor,
                    onColorSelected = { viewModel.updateThemeColor(it) }
                )
                TextSettingsItem(
                    title = stringResource(id = R.string.settings_grid_size),
                    subtitle = stringResource(id = R.string.settings_grid_size_columns, preferences!!.gridSize),
                    icon = Icons.Default.Menu,
                    onClick = { /* TODO: Slider or picker */ }
                )
            }

            // Sección Acerca de
            SettingsSection(title = stringResource(id = R.string.settings_section_about)) {
                TextSettingsItem(title = stringResource(id = R.string.settings_version), subtitle = "1.5.0", icon = Icons.Default.Info)
                TextSettingsItem(
                    title = stringResource(id = R.string.title_guides), 
                    icon = Icons.AutoMirrored.Filled.List,
                    onClick = onNavigateToGuides
                )
                TextSettingsItem(
                    title = stringResource(id = R.string.title_contact), 
                    icon = Icons.Default.Email,
                    onClick = onNavigateToContact
                )
            }
        }
    }
}

@Composable
fun UpdateUsernameDialog(
    currentValue: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.dialog_update_username_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = currentValue,
                    onValueChange = onValueChange,
                    label = { Text(stringResource(id = R.string.label_new_username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isLoading && currentValue.isNotBlank()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(stringResource(id = R.string.action_update))
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

@Composable
fun ChangePasswordDialog(
    currentPassword: String,
    password: String,
    repeatPassword: String,
    onCurrentPasswordChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRepeatPasswordChange: (String) -> Unit,
    isCurrentPasswordVisible: Boolean,
    isNewPasswordVisible: Boolean,
    isRepeatPasswordVisible: Boolean,
    onToggleCurrentVisibility: () -> Unit,
    onToggleNewVisibility: () -> Unit,
    onToggleRepeatVisibility: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(id = R.string.dialog_change_password_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentPassword,
                    onValueChange = onCurrentPasswordChange,
                    label = { Text(stringResource(id = R.string.label_current_password)) },
                    singleLine = true,
                    visualTransformation = if (isCurrentPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleCurrentVisibility) {
                            Icon(
                                imageVector = if (isCurrentPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(id = R.string.label_new_password)) },
                    singleLine = true,
                    visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleNewVisibility) {
                            Icon(
                                imageVector = if (isNewPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = onRepeatPasswordChange,
                    label = { Text(stringResource(id = R.string.label_repeat_password)) },
                    singleLine = true,
                    visualTransformation = if (isRepeatPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = onToggleRepeatVisibility) {
                            Icon(
                                imageVector = if (isRepeatPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = if (errorMessage.contains("Error") || errorMessage.contains("coinciden")) 
                            MaterialTheme.colorScheme.error 
                        else 
                            MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm, enabled = !isLoading && password.isNotBlank() && repeatPassword.isNotBlank() && currentPassword.isNotBlank()) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text(stringResource(id = R.string.action_update))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
            }) {
                Text(stringResource(id = R.string.action_cancel))
            }
        }
    )
}

@Composable
fun UserHeaderCard(
    isLoggedIn: Boolean,
    username: String,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (isLoggedIn) {
                    Text(text = stringResource(id = R.string.welcome_back), style = MaterialTheme.typography.bodyMedium)
                    Text(text = username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = stringResource(id = R.string.join_us), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row {
                        TextButton(onClick = onRegisterClick, contentPadding = PaddingValues(end = 8.dp)) {
                            Text(text = stringResource(id = R.string.register_now))
                        }
                        TextButton(onClick = onLoginClick) {
                            Text(text = stringResource(id = R.string.login_now))
                        }
                    }
                }
            }
            if (isLoggedIn) {
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = stringResource(id = R.string.action_logout),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
fun TextSettingsItem(
    title: String, 
    subtitle: String? = null, 
    icon: ImageVector, 
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit = {}
) {
    ListItem(
        headlineContent = { Text(title, color = color) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null, tint = color) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun ToggleSettingsItem(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, icon: ImageVector) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
fun ColorSettingsItem(selectedColor: String, onColorSelected: (String) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(id = R.string.settings_theme_color)) },
        leadingContent = { Icon(Icons.Default.Favorite, contentDescription = null) },
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ThemeColors.forEach { (name, color) ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == name) 2.dp else 0.dp,
                                color = if (selectedColor == name) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onColorSelected(name) }
                    )
                }
            }
        }
    )
}

@Composable
fun SyncStatusItem(isSynced: Boolean, onSyncClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(id = R.string.settings_sync_status)) },
        leadingContent = { Icon(Icons.Default.Refresh, contentDescription = null) },
        trailingContent = {
            IconButton(onClick = onSyncClick) {
                Icon(
                    imageVector = if (isSynced) Icons.Default.CheckCircle else Icons.Default.Close,
                    contentDescription = null,
                    tint = if (isSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    )
}
