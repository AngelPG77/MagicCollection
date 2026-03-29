package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState(initial = null)

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
            onRegisterClick = onNavigateToRegister
        )

        if (preferences != null) {
            // Sección General
            SettingsSection(title = "General") {
                SyncStatusItem(
                    isSynced = uiState.collections.all { it.synced },
                    onSyncClick = { viewModel.syncCollections() }
                )
            }

            // Sección Idioma
            SettingsSection(title = "Idioma") {
                TextSettingsItem(
                    title = "Idioma de búsqueda",
                    subtitle = if (preferences!!.searchLanguage == "en") "Inglés" else "Español",
                    icon = Icons.Default.Search,
                    onClick = { /* TODO: Dialog selector */ }
                )
                TextSettingsItem(
                    title = "Idioma de la app",
                    subtitle = if (preferences!!.appLanguage == "en") "English" else "Español",
                    icon = Icons.Default.Info,
                    onClick = { /* TODO: Flag selector */ }
                )
            }

            // Sección Apariencia
            SettingsSection(title = "Apariencia") {
                ToggleSettingsItem(
                    title = "Tema oscuro",
                    checked = preferences!!.darkTheme,
                    onCheckedChange = { viewModel.updateDarkTheme(it) },
                    icon = Icons.Default.Build
                )
                TextSettingsItem(
                    title = "Tamaño de cuadrícula",
                    subtitle = "${preferences!!.gridSize} columnas",
                    icon = Icons.Default.Menu,
                    onClick = { /* TODO: Slider or picker */ }
                )
            }

            // Sección Acerca de
            SettingsSection(title = "Acerca de") {
                TextSettingsItem(title = "Versión", subtitle = "1.5.0", icon = Icons.Default.Info)
                TextSettingsItem(title = "Guías y FAQ", icon = Icons.Default.List)
                TextSettingsItem(title = "Contacta con nosotros", icon = Icons.Default.Email)
            }
        }
    }
}

@Composable
fun UserHeaderCard(
    isLoggedIn: Boolean,
    username: String,
    onRegisterClick: () -> Unit
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
            Column {
                if (isLoggedIn) {
                    Text(text = "Bienvenido,", style = MaterialTheme.typography.bodyMedium)
                    Text(text = username, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                } else {
                    Text(text = "¡Únete a nosotros!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onRegisterClick, contentPadding = PaddingValues(0.dp)) {
                        Text(text = "Regístrate ahora")
                    }
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
fun TextSettingsItem(title: String, subtitle: String? = null, icon: ImageVector, onClick: () -> Unit = {}) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
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
fun SyncStatusItem(isSynced: Boolean, onSyncClick: () -> Unit) {
    ListItem(
        headlineContent = { Text("Perfil sincronizado") },
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
