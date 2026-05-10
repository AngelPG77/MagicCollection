package com.pga.magiccollection.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pga.magiccollection.R
import com.pga.magiccollection.ui.theme.LocalAppSpacing
import com.pga.magiccollection.ui.theme.LocalMtgSemanticColors

/**
 * Renders an MTG mana cost string (e.g. `{2}{W}{U}`) as a row of mana symbols.
 *
 * Single-color tokens (W, U, B, R, G, C) render the official `ic_mana_*` drawable so the
 * cost reads exactly as on a real card. Numeric and X/Y/Z tokens render as a neutral
 * colorless pip with the digit/letter inscribed.
 *
 * Hybrid (`W/U`) and Phyrexian (`W/P`) tokens fall back to the colorless pip with the raw
 * token text — a future pass can wire those to `ic_hybrid_*` for the ten Ravnica pairs.
 *
 * The whole row is exposed to accessibility services as the original cost string so screen
 * readers don't have to walk the pips one by one.
 */
@Composable
fun ManaCostRow(
    manaCost: String,
    modifier: Modifier = Modifier,
    pipSize: Dp = 18.dp
) {
    if (manaCost.isBlank()) return
    val tokens = parseManaCost(manaCost)
    if (tokens.isEmpty()) return

    val spacing = LocalAppSpacing.current

    Row(
        modifier = modifier.semantics { contentDescription = manaCost },
        horizontalArrangement = Arrangement.spacedBy(spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        tokens.forEach { token ->
            ManaPip(token = token, size = pipSize)
        }
    }
}

@Composable
private fun ManaPip(token: String, size: Dp) {
    val drawable = manaDrawableFor(token)
    if (drawable != null) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = null, // parent Row already exposes the full cost
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size)
        )
        return
    }

    // Generic / numeric / unknown token — neutral colorless pip with the raw text.
    val mtg = LocalMtgSemanticColors.current
    val upper = token.uppercase()
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(mtg.manaColorless)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = upper,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = (size.value * 0.58f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp,
                color = androidx.compose.ui.graphics.Color(0xFF1C1B1F)
            )
        )
    }
}

/**
 * Maps a single-letter MTG color/colorless token to its `ic_mana_*` drawable.
 * Returns null for everything else so the caller can render a textual fallback.
 */
@DrawableRes
internal fun manaDrawableFor(token: String): Int? = when (token.uppercase()) {
    "W" -> R.drawable.ic_mana_w
    "U" -> R.drawable.ic_mana_u
    "B" -> R.drawable.ic_mana_b
    "R" -> R.drawable.ic_mana_r
    "G" -> R.drawable.ic_mana_g
    "C" -> R.drawable.ic_mana_c
    else -> null
}

/**
 * Splits `{2}{W}{U}` into ["2", "W", "U"]. Tolerates whitespace and missing braces by
 * returning an empty list, in which case the row simply doesn't render.
 */
internal fun parseManaCost(input: String): List<String> {
    val cleaned = input.trim()
    if (cleaned.isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    var i = 0
    while (i < cleaned.length) {
        val ch = cleaned[i]
        if (ch == '{') {
            val end = cleaned.indexOf('}', i + 1)
            if (end == -1) break
            val token = cleaned.substring(i + 1, end).trim()
            if (token.isNotEmpty()) result.add(token)
            i = end + 1
        } else {
            i++
        }
    }
    return result
}
