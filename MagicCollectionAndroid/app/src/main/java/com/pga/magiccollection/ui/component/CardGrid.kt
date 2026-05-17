package com.pga.magiccollection.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.domain.model.search.IndexedCard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import com.pga.magiccollection.util.shimmerEffect

@Composable
fun CardGrid(
    cards: List<IndexedCard>,
    gridSize: Int,
    onCardClick: (IndexedCard) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(cards, key = { it.scryfallId }) { card ->
            CardItem(
                card = card,
                gridSize = gridSize,
                onClick = { onCardClick(card) }
            )
        }
    }
}

@Composable
fun PagedCardGrid(
    lazyPagingItems: LazyPagingItems<IndexedCard>,
    gridSize: Int,
    onCardClick: (IndexedCard) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = { index -> lazyPagingItems[index]?.scryfallId ?: index }
        ) { index ->
            val card = lazyPagingItems[index]
            if (card != null) {
                CardItem(
                    card = card,
                    gridSize = gridSize,
                    onClick = { onCardClick(card) }
                )
            } else {
                // Placeholder para carga de paginación
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.718f)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerEffect()
                )
            }
        }
    }
}

@Composable
fun CardItem(
    card: IndexedCard,
    gridSize: Int,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Sutil efecto de elevación visual al interactuar
            }
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.718f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().shimmerEffect())
            }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(card.imageUrl)
                    .size(360, 502)
                    .precision(Precision.INEXACT)
                    .crossfade(true)
                    .build(),
                contentDescription = card.name,
                onSuccess = { isLoading = false },
                onError = { isLoading = false },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Mana cost — discreet pip strip top-left when grid is sparse enough.
            // 5+ columns are too tight; 1–4 columns get the overlay.
            if (gridSize <= 4 && !card.manaCost.isNullOrBlank() && !isLoading) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    ManaCostRow(
                        manaCost = card.manaCost,
                        pipSize = if (gridSize <= 2) 18.dp else 14.dp
                    )
                }
            }

            // Rarity badge — bottom-right, only on lower densities where it fits.
            if (gridSize <= 3 && !isLoading) {
                CardRarity.fromRaw(card.rarity)?.let { rarity ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        RarityBadge(rarity = rarity)
                    }
                }
            }
        }
        
        // Solo mostrar texto si la grid no es muy densa (> 4 columnas)
        if (gridSize <= 4) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = card.name,
                style = if (gridSize <= 2) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
