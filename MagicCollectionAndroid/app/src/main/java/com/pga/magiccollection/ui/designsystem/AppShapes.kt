package com.pga.magiccollection.ui.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Immutable
data class AppShapes(
    val none: Shape = RoundedCornerShape(0.dp),
    val extraSmall: Shape = RoundedCornerShape(4.dp),
    val small: Shape = RoundedCornerShape(8.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(16.dp),
    val extraLarge: Shape = RoundedCornerShape(28.dp),
    val full: Shape = RoundedCornerShape(100.dp),
    val card: Shape = RoundedCornerShape(12.dp),
    val modal: Shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
)

val LocalAppShapes = staticCompositionLocalOf { AppShapes() }
