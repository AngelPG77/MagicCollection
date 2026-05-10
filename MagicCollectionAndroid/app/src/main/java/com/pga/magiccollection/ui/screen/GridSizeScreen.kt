package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pga.magiccollection.R

@Composable
fun GridSizeScreen(viewModel: MainViewModel) {
    val preferences by viewModel.preferences.collectAsStateWithLifecycle(initialValue = null)
    
    // Use a local state for immediate feedback, but sync with viewModel
    var gridSize by remember(preferences?.gridSize) { mutableIntStateOf(preferences?.gridSize ?: 3) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.settings_grid_size_preview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Grid Preview
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
                .padding(8.dp),
            contentAlignment = Alignment.TopStart
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                userScrollEnabled = false,
                modifier = Modifier.fillMaxSize()
            ) {
                items(gridSize * 3) { 
                    CardPlaceholder()
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.settings_grid_size_selector),
            style = MaterialTheme.typography.bodyLarge
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            FilledIconButton(
                onClick = {
                    if (gridSize > 1) {
                        gridSize--
                        viewModel.updateGridSize(gridSize)
                    }
                },
                enabled = gridSize > 1
            ) {
                Icon(Icons.Default.Remove, contentDescription = null)
            }

            Text(
                text = gridSize.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            FilledIconButton(
                onClick = {
                    if (gridSize < 6) {
                        gridSize++
                        viewModel.updateGridSize(gridSize)
                    }
                },
                enabled = gridSize < 6
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
        
        Text(
            text = stringResource(id = R.string.settings_grid_size_columns, gridSize),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun CardPlaceholder() {
    Card(
        modifier = Modifier
            .aspectRatio(0.7f) // Typical card aspect ratio
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Using a generic icon as placeholder
            Icon(
                painter = painterResource(id = android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
