package com.pga.magiccollection.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MagicCollectionSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    actionColor: Color = MaterialTheme.colorScheme.primary
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier.padding(12.dp),
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = actionColor,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun MagicCollectionSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { MagicCollectionSnackbar(it) }
    )
}
