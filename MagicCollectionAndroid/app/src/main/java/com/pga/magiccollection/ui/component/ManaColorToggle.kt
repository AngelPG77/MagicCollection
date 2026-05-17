package com.pga.magiccollection.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pga.magiccollection.R

private val UnselectedAlpha = 0.32f
private val UnselectedScale = 0.86f

/**
 * Single-color mana selector rendered as the bare mana symbol — no chip frame around it.
 *
 * Selected state: full opacity and full size. Unselected: dimmed and slightly scaled down
 * so the active colors read at a glance. The touch target is a 48dp circle around the
 * visible icon to satisfy WCAG minimum target size.
 *
 * @param color one of "W", "U", "B", "R", "G", "C"; unknown tokens render nothing
 * @param selected whether this color is currently part of the active filter
 * @param onClick invoked when the icon is tapped; the caller toggles the filter state
 * @param iconSize visible size of the mana symbol; touch target stays at 48dp regardless
 */
@Composable
fun ManaColorToggle(
    color: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 36.dp
) {
    val drawable = manaDrawableFor(color) ?: return

    val alpha by animateFloatAsState(
        targetValue = if (selected) 1f else UnselectedAlpha,
        label = "manaToggleAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else UnselectedScale,
        label = "manaToggleScale"
    )

    val stateLabel = stringResource(
        id = if (selected) R.string.state_selected else R.string.state_not_selected
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 24.dp),
                role = Role.Checkbox,
                onClick = onClick
            )
            .semantics { stateDescription = stateLabel },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = color,
            modifier = Modifier
                .size(iconSize)
                .scale(scale)
                .alpha(alpha)
        )
    }
}
