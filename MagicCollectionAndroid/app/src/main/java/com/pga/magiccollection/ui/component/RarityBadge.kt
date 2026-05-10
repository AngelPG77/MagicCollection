package com.pga.magiccollection.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.ui.theme.LocalAppSpacing
import com.pga.magiccollection.ui.theme.LocalMtgSemanticColors

/**
 * The four MTG rarities, ordered from common to mythic.
 * Mirrors the strings used by Scryfall (`common`, `uncommon`, `rare`, `mythic`).
 */
enum class CardRarity(val rawValue: String) {
    Common("common"),
    Uncommon("uncommon"),
    Rare("rare"),
    Mythic("mythic");

    companion object {
        fun fromRaw(raw: String?): CardRarity? =
            raw?.let { value -> entries.firstOrNull { it.rawValue.equals(value, ignoreCase = true) } }
    }
}

/**
 * Pill badge showing a card's rarity. Color follows the canonical MTG rarity palette
 * (gem-frame colors), which is independent of the user's guild theme — rarity is sacred.
 */
@Composable
fun RarityBadge(
    rarity: CardRarity,
    modifier: Modifier = Modifier
) {
    val colors = LocalMtgSemanticColors.current
    val spacing = LocalAppSpacing.current
    val (background, foreground) = when (rarity) {
        CardRarity.Common -> colors.rarityCommon to contrastingTextOn(colors.rarityCommon)
        CardRarity.Uncommon -> colors.rarityUncommon to contrastingTextOn(colors.rarityUncommon)
        CardRarity.Rare -> colors.rarityRare to contrastingTextOn(colors.rarityRare)
        CardRarity.Mythic -> colors.rarityMythic to contrastingTextOn(colors.rarityMythic)
    }

    Text(
        text = rarity.rawValue.replaceFirstChar { it.titlecase() },
        style = MaterialTheme.typography.labelSmall,
        color = foreground,
        modifier = modifier
            .clip(RoundedCornerShape(spacing.xs))
            .background(background)
            .padding(horizontal = spacing.sm, vertical = 2.dp)
    )
}

private fun contrastingTextOn(background: Color): Color {
    // Quick luminance check: perceived brightness threshold ~0.55
    val r = background.red
    val g = background.green
    val b = background.blue
    val luminance = 0.299f * r + 0.587f * g + 0.114f * b
    return if (luminance > 0.55f) Color(0xFF1A1A1A) else Color.White
}
