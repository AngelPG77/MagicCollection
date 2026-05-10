package com.pga.magiccollection.ui.component

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pga.magiccollection.ui.theme.LocalAppSpacing
import com.pga.magiccollection.ui.theme.LocalMtgSemanticColors

/**
 * Renders an MTG mana cost string (e.g. `{2}{W}{U}`) as a row of colored pips.
 *
 * Tokens recognized today:
 *  - Single-color symbols: `W`, `U`, `B`, `R`, `G`, `C`
 *  - Generic numeric: `0`–`20`, `X`
 *  - Anything else falls back to a neutral pip with the literal token text.
 *
 * Hybrid (`W/U`) and Phyrexian (`W/P`) symbols render as the neutral fallback for now —
 * a future iteration can split-color the pip the same way [GuildBadge] does.
 *
 * The whole row is exposed to accessibility services as the original cost string so
 * screen readers don't have to walk the pips one by one.
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
    val mtg = LocalMtgSemanticColors.current
    val upper = token.uppercase()
    val (bg, fg, label) = when (upper) {
        "W" -> Triple(mtg.manaWhite, Color(0xFF1A1A1A), "W")
        "U" -> Triple(mtg.manaBlue, Color.White, "U")
        "B" -> Triple(mtg.manaBlack, Color.White, "B")
        "R" -> Triple(mtg.manaRed, Color.White, "R")
        "G" -> Triple(mtg.manaGreen, Color.White, "G")
        "C" -> Triple(mtg.manaColorless, Color(0xFF1A1A1A), "C")
        "X", "Y", "Z" -> Triple(mtg.manaColorless, Color(0xFF1A1A1A), upper)
        else -> {
            // Generic numeric cost — render the number on the colorless background.
            val isNumeric = upper.toIntOrNull() != null
            if (isNumeric) {
                Triple(mtg.manaColorless, Color(0xFF1A1A1A), upper)
            } else {
                // Hybrid (e.g. "W/U") or unknown — fall back to a faint pill with the
                // raw token text, so the cost is still readable.
                Triple(mtg.manaColorless, Color(0xFF1A1A1A), upper)
            }
        }
    }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(bg)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = fg,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = (size.value * 0.58f).sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            )
        )
    }
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
