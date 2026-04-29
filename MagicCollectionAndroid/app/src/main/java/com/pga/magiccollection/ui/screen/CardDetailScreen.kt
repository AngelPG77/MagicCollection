package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto

@Composable
fun CardDetailScreen(
    card: ScryfallCardDto?,
    isLoading: Boolean = false,
    onBackClick: () -> Unit,
    onVersionClick: (String) -> Unit,
    versions: List<ScryfallCardDto> = emptyList()
) {
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (card == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(id = R.string.card_detail_not_found))
        }
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.card_detail_versions),
        stringResource(id = R.string.card_detail_rules)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Card Image - prioritize PNG for transparency support
        AsyncImage(
            model = card.imageUris?.png ?: card.imageUris?.large ?: card.imageUris?.normal,
            contentDescription = card.name,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.718f) // Standard Magic card ratio
                .padding(16.dp),
            contentScale = ContentScale.Fit
        )

        // Card Info Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Header Row: Name and Mana Cost
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = card.printedName ?: card.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = card.manaCost ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Power/Toughness (if creature)
            if (!card.power.isNullOrEmpty() && !card.toughness.isNullOrEmpty()) {
                Text(
                    text = "${card.power}/${card.toughness}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Type Line
            Text(
                text = card.typeLine ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Oracle Text
            Text(
                text = card.oracleText ?: "",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tabs
            SecondaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Tab Content
            Box(modifier = Modifier.heightIn(min = 200.dp)) {
                when (selectedTabIndex) {
                    0 -> VersionsTab(versions, onVersionClick)
                    1 -> RulesTab()
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun VersionsTab(versions: List<ScryfallCardDto>, onVersionClick: (String) -> Unit) {
    val context = LocalContext.current
    if (versions.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stringResource(id = R.string.card_detail_no_versions))
        }
    } else {
        LazyRow(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            items(versions) { version ->
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .clickable { version.scryfallId?.let { onVersionClick(it) } },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(version.imageUris?.normal ?: version.imageUris?.small)
                                .size(320, 446)
                                .precision(Precision.INEXACT)
                                .crossfade(false)
                                .build(),
                            contentDescription = version.setCode,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.718f),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            text = version.setCode?.uppercase() ?: "",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RulesTab() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(id = R.string.work_in_progress),
                style = MaterialTheme.typography.titleLarge
            )
            Text(text = stringResource(id = R.string.wip_description))
        }
    }
}
