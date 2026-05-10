package com.pga.magiccollection.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Spacing scale for the app. Use these tokens instead of inlining `.dp` literals so the
 * UI stays consistent and a global scale change is one-line.
 *
 * Naming follows a t-shirt scale; semantic aliases are added as we discover real recurring
 * uses across screens.
 */
@Immutable
data class AppSpacing(
    val none: Dp = 0.dp,
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
    val xxxl: Dp = 48.dp,

    // Semantic aliases (read sites)
    val screenHorizontal: Dp = 16.dp,
    val screenVertical: Dp = 16.dp,
    val sectionGap: Dp = 24.dp,
    val cardGap: Dp = 12.dp,
    val listItemVertical: Dp = 8.dp
)

@Immutable
data class AppDimens(
    val touchTargetMin: Dp = 48.dp,
    val iconSm: Dp = 16.dp,
    val iconMd: Dp = 24.dp,
    val iconLg: Dp = 32.dp,
    val cornerXs: Dp = 4.dp,
    val cornerSm: Dp = 8.dp,
    val cornerMd: Dp = 12.dp,
    val cornerLg: Dp = 16.dp,
    val cornerXl: Dp = 24.dp,
    val guildBadge: Dp = 36.dp,
    val divider: Dp = 1.dp
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }
val LocalAppDimens = staticCompositionLocalOf { AppDimens() }
