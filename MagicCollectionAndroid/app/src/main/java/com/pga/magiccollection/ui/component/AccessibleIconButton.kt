package com.pga.magiccollection.ui.component

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Drop-in replacement for [IconButton] that *requires* a non-blank content description and
 * surfaces it both as the button's onClickLabel and the icon's contentDescription. Use this
 * everywhere we tap on an icon — it removes the easy mistake of `contentDescription = null`
 * on actionable elements.
 *
 * For purely decorative icons inside larger widgets, keep using [Icon] directly with
 * `contentDescription = null` — that case is intentional, not an oversight.
 *
 * The 48dp minimum touch target comes for free from [IconButton]'s default sizing.
 */
@Composable
fun AccessibleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = LocalContentColor.current,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors()
) {
    require(contentDescription.isNotBlank()) {
        "AccessibleIconButton requires a non-blank contentDescription. " +
                "If the icon is purely decorative, use Icon(...) directly inside a parent " +
                "that already exposes a label to accessibility services."
    }
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    ) {
        Icon(imageVector = icon, contentDescription = contentDescription, tint = tint)
    }
}
