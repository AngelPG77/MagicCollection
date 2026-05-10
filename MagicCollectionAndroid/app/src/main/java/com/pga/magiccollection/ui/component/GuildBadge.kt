package com.pga.magiccollection.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.ui.theme.Guild
import com.pga.magiccollection.ui.theme.LocalAppDimens

/**
 * A circular swatch divided diagonally into the two MTG colors that compose a [Guild].
 *
 * Used in Settings as the guild-picker. Wraps in [minimumInteractiveComponentSize] so the
 * touch target meets the 48dp accessibility floor regardless of visual size.
 */
@Composable
fun GuildBadge(
    guild: Guild,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dimens = LocalAppDimens.current
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
    val borderWidth = if (selected) 3.dp else 1.dp

    Canvas(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(dimens.guildBadge)
            .clip(CircleShape)
            .border(width = borderWidth, color = borderColor, shape = CircleShape)
            .clickable(onClickLabel = guild.displayName, onClick = onClick)
    ) {
        val s = Size(size.width, size.height)
        // Top-left half = guild.primary, bottom-right half = guild.secondary
        val diagonal = Path().apply {
            moveTo(0f, 0f)
            lineTo(s.width, 0f)
            lineTo(0f, s.height)
            close()
        }
        drawRect(color = guild.secondary, topLeft = Offset.Zero, size = s)
        drawPath(diagonal, color = guild.primary)
    }
}
