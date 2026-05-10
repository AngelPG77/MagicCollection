package com.pga.magiccollection.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.pga.magiccollection.R
import com.pga.magiccollection.data.remote.dto.ScryfallCardDto
import com.pga.magiccollection.ui.component.CardDetailSkeleton
import com.pga.magiccollection.ui.component.CardRarity
import com.pga.magiccollection.ui.component.EmptyState
import com.pga.magiccollection.ui.component.ManaCostRow
import com.pga.magiccollection.ui.component.RarityBadge
import com.pga.magiccollection.ui.theme.LocalAppDimens
import com.pga.magiccollection.ui.theme.LocalAppSpacing

@Composable
fun CardDetailScreen(
    card: ScryfallCardDto?,
    isLoading: Boolean = false,
    onBackClick: () -> Unit,
    onVersionClick: (String) -> Unit,
    versions: List<ScryfallCardDto> = emptyList()
) {
    if (isLoading) {
        CardDetailSkeleton()
        return
    }

    if (card == null) {
        EmptyState(
            title = stringResource(id = R.string.card_detail_not_found),
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val spacing = LocalAppSpacing.current
    val dimens = LocalAppDimens.current
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(id = R.string.card_detail_versions),
        stringResource(id = R.string.card_detail_rules)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = spacing.xxl)
    ) {
        // Hero card image — centered, slightly raised feel via the surrounding background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.lg),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = card.imageUris?.png ?: card.imageUris?.large ?: card.imageUris?.normal,
                contentDescription = card.name,
                modifier = Modifier
                    .fillMaxWidth(0.78f)
                    .aspectRatio(0.718f)
                    .clip(RoundedCornerShape(dimens.cornerLg)),
                contentScale = ContentScale.Fit
            )
        }

        // Title + mana cost
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = card.printedName ?: card.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            ManaCostRow(manaCost = card.manaCost ?: "", pipSize = 22.dp)
        }

        // Type line + rarity
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = card.typeLine ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic,
                modifier = Modifier.weight(1f)
            )
            CardRarity.fromRaw(card.rarity)?.let { rarity ->
                RarityBadge(rarity = rarity)
            }
        }

        // Power / Toughness chip (creatures only)
        if (!card.power.isNullOrEmpty() && !card.toughness.isNullOrEmpty()) {
            Box(
                modifier = Modifier
                    .padding(horizontal = spacing.lg, vertical = spacing.xs)
                    .clip(RoundedCornerShape(dimens.cornerSm))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = spacing.md, vertical = spacing.xs)
            ) {
                Text(
                    text = "${card.power} / ${card.toughness}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.md))

        // Oracle text in a card-style container, matching the rest of the app
        if (!card.oracleText.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.lg),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(dimens.cornerLg)
            ) {
                Text(
                    text = card.oracleText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(spacing.lg)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.lg))

        // Tabs — versions / rules
        SecondaryTabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.padding(horizontal = spacing.sm),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.lg)
            .heightIn(min = 200.dp)
        ) {
            when (selectedTabIndex) {
                0 -> VersionsTab(versions, onVersionClick)
                1 -> RulesTab()
            }
        }
    }
}

@Composable
private fun VersionsTab(versions: List<ScryfallCardDto>, onVersionClick: (String) -> Unit) {
    val context = LocalContext.current
    val spacing = LocalAppSpacing.current
    val dimens = LocalAppDimens.current

    if (versions.isEmpty()) {
        EmptyState(
            title = stringResource(id = R.string.card_detail_no_versions),
            modifier = Modifier.fillMaxWidth()
        )
        return
    }

    LazyRow(
        modifier = Modifier.padding(vertical = spacing.md),
        horizontalArrangement = Arrangement.spacedBy(spacing.md)
    ) {
        items(versions) { version ->
            Card(
                modifier = Modifier
                    .width(140.dp)
                    .clickable { version.scryfallId?.let { onVersionClick(it) } },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                shape = RoundedCornerShape(dimens.cornerMd)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(spacing.sm)
                    )
                }
            }
        }
    }
}

@Composable
private fun RulesTab() {
    val spacing = LocalAppSpacing.current
    EmptyState(
        title = stringResource(id = R.string.work_in_progress),
        message = stringResource(id = R.string.wip_description),
        modifier = Modifier
            .fillMaxWidth()
            .padding(spacing.lg)
    )
}
