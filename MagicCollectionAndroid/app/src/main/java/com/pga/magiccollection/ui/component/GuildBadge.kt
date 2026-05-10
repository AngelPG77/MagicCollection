package com.pga.magiccollection.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.ui.theme.Guild
import com.pga.magiccollection.ui.theme.LocalAppDimens

/**
 * Circular swatch showing the guild's official hybrid mana symbol (Azorius =
 * `ic_hybrid_wu`, Boros = `ic_hybrid_rw`, etc.). Used in Settings as the guild picker.
 *
 * The container is wrapped in [minimumInteractiveComponentSize] so the touch target meets
 * the 48dp accessibility floor regardless of the visual size.
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

    Image(
        painter = painterResource(id = guild.hybridIconRes),
        contentDescription = guild.displayName,
        contentScale = ContentScale.Fit,
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(dimens.guildBadge)
            .clip(CircleShape)
            .border(width = borderWidth, color = borderColor, shape = CircleShape)
            .clickable(onClickLabel = guild.displayName, onClick = onClick)
            .padding(2.dp)
    )
}
