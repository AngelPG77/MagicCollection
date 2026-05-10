package com.pga.magiccollection.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.ui.theme.LocalAppDimens
import com.pga.magiccollection.ui.theme.LocalAppSpacing
import com.pga.magiccollection.util.shimmerEffect

/**
 * Card-shaped skeleton used while a card detail is loading. Mirrors the real layout:
 *  - Card image silhouette at the top
 *  - Title placeholder + 3 mana pips
 *  - Type-line + rarity badge placeholders
 *  - Three lines of body text for the oracle text
 *  - A row of small card thumbnails (versions tab)
 *
 * Every block uses [shimmerEffect] so the surface looks alive instead of "frozen".
 */
@Composable
fun CardDetailSkeleton(modifier: Modifier = Modifier) {
    val spacing = LocalAppSpacing.current
    val dimens = LocalAppDimens.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = spacing.lg),
        verticalArrangement = Arrangement.spacedBy(spacing.lg)
    ) {
        // Card image
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .align(Alignment.CenterHorizontally)
                .padding(top = spacing.lg)
                .aspectRatio(0.718f)
                .clip(RoundedCornerShape(dimens.cornerLg))
                .shimmerEffect()
        )

        // Title row + mana pips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp)
                    .clip(RoundedCornerShape(dimens.cornerSm))
                    .shimmerEffect()
            )
            repeat(3) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .shimmerEffect()
                )
            }
        }

        // Type line + rarity badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .clip(RoundedCornerShape(dimens.cornerSm))
                    .shimmerEffect()
            )
            Box(
                modifier = Modifier
                    .width(72.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(dimens.cornerSm))
                    .shimmerEffect()
            )
        }

        // Oracle text — 3 lines
        Column(verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (index == 2) 0.6f else 1f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(dimens.cornerXs))
                        .shimmerEffect()
                )
            }
        }

        // Versions strip
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing.md)
        ) {
            items(List(4) { it }) {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .aspectRatio(0.718f)
                        .clip(RoundedCornerShape(dimens.cornerLg))
                        .shimmerEffect()
                )
            }
        }
    }
}
