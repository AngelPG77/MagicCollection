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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.domain.model.search.IndexedCard

@Composable
fun CardGrid(
    cards: List<IndexedCard>,
    gridSize: Int,
    onCardClick: (IndexedCard) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp)
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cards, key = { it.scryfallId }) { card ->
            CardItem(
                card = card,
                onClick = { onCardClick(card) }
            )
        }
    }
}

@Composable
fun CardItem(
    card: IndexedCard,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.718f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(card.imageUrl)
                    .size(360, 502)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .build(),
                contentDescription = card.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = card.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
