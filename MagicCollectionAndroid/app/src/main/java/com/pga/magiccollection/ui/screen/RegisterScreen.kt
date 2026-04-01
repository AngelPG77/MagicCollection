package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R

import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun RegisterScreen(
    viewModel: MainViewModel,
    initialLoginMode: Boolean,
    onSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    // reset mode when route changes
    var isLoginMode by remember(initialLoginMode) { mutableStateOf(initialLoginMode) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Si el login es exitoso, volvemos atrás o a la pantalla principal
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) stringResource(id = R.string.login_title) else stringResource(id = R.string.register_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = if (isLoginMode) stringResource(id = R.string.login_subtitle) else stringResource(id = R.string.register_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = uiState.usernameInput,
            onValueChange = { viewModel.onUsernameChanged(it) },
            label = { Text(stringResource(id = R.string.label_username)) },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = uiState.passwordInput,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = { Text(stringResource(id = R.string.label_password)) },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (uiState.authMessage != null) {
            Text(
                text = uiState.authMessage!!,
                color = if (uiState.authMessage!!.contains("Error") || 
                    uiState.authMessage!!.contains("No") || 
                    uiState.authMessage!!.contains("incorrectos") ||
                    uiState.authMessage!!.contains("Prohibido") ||
                    uiState.authMessage!!.contains("existe")) 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { if (isLoginMode) viewModel.login() else viewModel.register() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !uiState.authLoading && uiState.usernameInput.isNotBlank() && uiState.passwordInput.isNotBlank()
        ) {
            if (uiState.authLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(if (isLoginMode) stringResource(id = R.string.login_button) else stringResource(id = R.string.register_button))
            }
        }

        TextButton(
            onClick = { 
                isLoginMode = !isLoginMode 
                // Limpiar mensaje al cambiar de modo
                viewModel.onUsernameChanged(uiState.usernameInput)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                if (isLoginMode) stringResource(id = R.string.login_footer)
                else stringResource(id = R.string.register_footer)
            )
        }
    }
}
