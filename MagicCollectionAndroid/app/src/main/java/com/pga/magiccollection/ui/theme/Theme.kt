package com.pga.magiccollection.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

private fun Color.blend(other: Color, ratio: Float): Color {
    return Color(ColorUtils.blendARGB(this.toArgb(), other.toArgb(), ratio))
}

private fun getColorScheme(baseColor: Color, isDark: Boolean): androidx.compose.material3.ColorScheme {
    val primary = baseColor
    val secondary = baseColor.blend(Color.Gray, 0.4f)
    val tertiary = baseColor.blend(Color(0xFF9E9E9E), 0.5f)

    return if (isDark) {
        val primaryContainer = baseColor.blend(Color.Black, 0.75f)
        val onPrimaryContainer = baseColor.blend(Color.White, 0.85f)
        val secondaryContainer = secondary.blend(Color.Black, 0.75f)
        val onSecondaryContainer = secondary.blend(Color.White, 0.85f)

        darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            tertiaryContainer = tertiary.blend(Color.Black, 0.75f),
            onTertiaryContainer = tertiary.blend(Color.White, 0.85f)
        )
    } else {
        val primaryContainer = baseColor.blend(Color.White, 0.50f)
        val onPrimaryContainer = baseColor.blend(Color.Black, 0.80f)
        val secondaryContainer = secondary.blend(Color.White, 0.50f)
        val onSecondaryContainer = secondary.blend(Color.Black, 0.80f)

        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            tertiaryContainer = tertiary.blend(Color.White, 0.50f),
            onTertiaryContainer = tertiary.blend(Color.Black, 0.80f)
        )
    }
}

val ThemeColors = mapOf(
    "Purple" to Color(0xFF9C27B0), // Púrpura vibrante (Material Purple 500)
    "Blue" to Color(0xFF2196F3),
    "Red" to Color(0xFFF44336),
    "Green" to Color(0xFF4CAF50),
    "Orange" to Color(0xFFFF9800)
)

@Composable
fun MagicCollectionAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: String = "Purple",
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val baseColor = ThemeColors[themeColor] ?: ThemeColors["Purple"]!!
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && themeColor == "Purple" -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> getColorScheme(baseColor, darkTheme)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}