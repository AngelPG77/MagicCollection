package com.pga.magiccollection.ui.theme

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext

/**
 * Root theme for the Magic Collection app.
 *
 * The active [Guild] drives `primary`, `secondary`, `tertiary` and their containers
 * (the brand identity). The `dynamicSurfaces` flag opts into Material You for the *neutral*
 * surface tokens only — the guild's brand colors are never overridden by the system
 * wallpaper.
 *
 * Brand-color transitions between guilds are animated so a settings change feels like a
 * theme switch rather than a flash.
 */
@Composable
fun MagicCollectionAppTheme(
    guild: Guild = Guild.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicSurfaces: Boolean = true,
    content: @Composable () -> Unit
) {
    val guildScheme = GuildSchemes.of(guild, darkTheme)
    val materialYouScheme = if (dynamicSurfaces && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else null

    val baseScheme = if (materialYouScheme != null) {
        applyMaterialYouSurfaces(guildScheme, materialYouScheme)
    } else guildScheme

    val animatedScheme = animateBrandColors(baseScheme)
    val semanticColors = semanticColorsFor(darkTheme)

    CompositionLocalProvider(
        LocalMtgSemanticColors provides semanticColors,
        LocalAppSpacing provides AppSpacing(),
        LocalAppDimens provides AppDimens()
    ) {
        MaterialTheme(
            colorScheme = animatedScheme,
            typography = Typography,
            content = content
        )
    }
}

/**
 * Copies only the *neutral* surface tokens from [materialYou] onto [guildScheme].
 * Brand tokens (primary, secondary, tertiary, error and their on/container variants)
 * stay from the guild.
 */
private fun applyMaterialYouSurfaces(
    guildScheme: ColorScheme,
    materialYou: ColorScheme
): ColorScheme = guildScheme.copy(
    background = materialYou.background,
    onBackground = materialYou.onBackground,
    surface = materialYou.surface,
    onSurface = materialYou.onSurface,
    surfaceVariant = materialYou.surfaceVariant,
    onSurfaceVariant = materialYou.onSurfaceVariant,
    surfaceTint = guildScheme.primary,
    surfaceBright = materialYou.surfaceBright,
    surfaceDim = materialYou.surfaceDim,
    surfaceContainer = materialYou.surfaceContainer,
    surfaceContainerLow = materialYou.surfaceContainerLow,
    surfaceContainerLowest = materialYou.surfaceContainerLowest,
    surfaceContainerHigh = materialYou.surfaceContainerHigh,
    surfaceContainerHighest = materialYou.surfaceContainerHighest,
    inverseSurface = materialYou.inverseSurface,
    inverseOnSurface = materialYou.inverseOnSurface,
    outline = materialYou.outline,
    outlineVariant = materialYou.outlineVariant,
    scrim = materialYou.scrim
)

/**
 * Animates the brand-facing color tokens (the ones that change when the user picks a
 * different guild). Surface/neutral tokens snap because they don't change with the guild.
 */
@Composable
private fun animateBrandColors(target: ColorScheme): ColorScheme {
    val spec = tween<androidx.compose.ui.graphics.Color>(durationMillis = 320)

    val primary by animateColorAsState(target.primary, spec, label = "primary")
    val onPrimary by animateColorAsState(target.onPrimary, spec, label = "onPrimary")
    val primaryContainer by animateColorAsState(target.primaryContainer, spec, label = "primaryContainer")
    val onPrimaryContainer by animateColorAsState(target.onPrimaryContainer, spec, label = "onPrimaryContainer")
    val inversePrimary by animateColorAsState(target.inversePrimary, spec, label = "inversePrimary")

    val secondary by animateColorAsState(target.secondary, spec, label = "secondary")
    val onSecondary by animateColorAsState(target.onSecondary, spec, label = "onSecondary")
    val secondaryContainer by animateColorAsState(target.secondaryContainer, spec, label = "secondaryContainer")
    val onSecondaryContainer by animateColorAsState(target.onSecondaryContainer, spec, label = "onSecondaryContainer")

    val tertiary by animateColorAsState(target.tertiary, spec, label = "tertiary")
    val onTertiary by animateColorAsState(target.onTertiary, spec, label = "onTertiary")
    val tertiaryContainer by animateColorAsState(target.tertiaryContainer, spec, label = "tertiaryContainer")
    val onTertiaryContainer by animateColorAsState(target.onTertiaryContainer, spec, label = "onTertiaryContainer")

    return target.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        inversePrimary = inversePrimary,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        tertiaryContainer = tertiaryContainer,
        onTertiaryContainer = onTertiaryContainer
    )
}
