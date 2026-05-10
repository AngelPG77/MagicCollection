package com.pga.magiccollection.util

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import com.pga.magiccollection.domain.model.card.ColorMask
import com.pga.magiccollection.ui.theme.*

fun Modifier.shimmerEffect(): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    
    // Guild-aware shimmer base
    val baseColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val highlightColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.15f else 0.1f)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    val brush = Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    background(brush)
}

/**
 * Modern hybrid background with linear gradient
 */
fun Modifier.hybridBackground(color1: Color, color2: Color) = this.drawBehind {
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(color1, color2),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height)
        )
    )
}

/**
 * Maps MTG color mask to a Guild if it represents a hybrid pair
 */
fun Int.toGuild(): Guild {
    val colors = mutableListOf<String>()
    if ((this and ColorMask.WHITE) != 0) colors.add("W")
    if ((this and ColorMask.BLUE) != 0) colors.add("U")
    if ((this and ColorMask.BLACK) != 0) colors.add("B")
    if ((this and ColorMask.RED) != 0) colors.add("R")
    if ((this and ColorMask.GREEN) != 0) colors.add("G")
    
    return Guild.fromColors(colors) ?: Guild.Default
}

/**
 * Maps MTG color mask to the dominant Compose color for theming
 */
fun Int.toManaColor(): Color {
    return when {
        // Prioridad: Multicolores -> Oro
        (this and (this - 1)) != 0 -> ManaGold
        (this and ColorMask.WHITE) != 0 -> ManaWhitePrimary
        (this and ColorMask.BLUE) != 0 -> ManaBluePrimary
        (this and ColorMask.BLACK) != 0 -> ManaBlackPrimary
        (this and ColorMask.RED) != 0 -> ManaRedPrimary
        (this and ColorMask.GREEN) != 0 -> ManaGreenPrimary
        (this and ColorMask.COLORLESS) != 0 -> ManaColorlessPrimary
        else -> Color(0xFF9C27B0) // Default Purple
    }
}
