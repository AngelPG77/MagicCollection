package com.pga.magiccollection.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/**
 * Builds Material 3 [ColorScheme] objects from a [Guild]'s two canonical MTG colors.
 *
 * Strategy:
 *  - `primary`  = blend of guild.primary + guild.secondary, biased toward the non-white
 *                 color when one of them is white (white-as-primary does not work on light
 *                 surfaces).
 *  - `secondary` = the second MTG color of the pair, desaturated slightly.
 *  - `tertiary`  = a desaturated mix used for accents and chips.
 *
 * Schemes are cached per (guild, isDark) pair so recomposition does not recompute them.
 */
object GuildSchemes {
    private val cache = mutableMapOf<Pair<Guild, Boolean>, ColorScheme>()

    fun of(guild: Guild, isDark: Boolean): ColorScheme {
        return cache.getOrPut(guild to isDark) { build(guild, isDark) }
    }

    private fun build(guild: Guild, isDark: Boolean): ColorScheme {
        val brand = brandColor(guild)
        val accent = accentColor(guild)

        return if (isDark) buildDark(brand, accent) else buildLight(brand, accent)
    }

    /**
     * Picks the more visible of the guild's two colors. White-led guilds (Azorius, Orzhov,
     * Selesnya, Boros) bias toward the partner color since white-on-white surfaces fail.
     */
    private fun brandColor(guild: Guild): Color {
        val a = guild.primary
        val b = guild.secondary
        return when {
            a == MtgManaColor.White -> b.blend(a, 0.20f)
            b == MtgManaColor.White -> a.blend(b, 0.20f)
            else -> a.blend(b, 0.50f)
        }
    }

    private fun accentColor(guild: Guild): Color {
        val a = guild.primary
        val b = guild.secondary
        return when {
            a == MtgManaColor.White -> a
            b == MtgManaColor.White -> b
            else -> b
        }
    }

    private fun buildLight(brand: Color, accent: Color): ColorScheme {
        val primary = brand
        val secondary = accent.blend(Color(0xFF606060), 0.25f)
        val tertiary = brand.blend(accent, 0.50f).blend(Color(0xFFB0B0B0), 0.30f)

        return lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.blend(Color.White, 0.78f),
            onPrimaryContainer = primary.blend(Color.Black, 0.55f),
            inversePrimary = primary.blend(Color.White, 0.50f),

            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.blend(Color.White, 0.78f),
            onSecondaryContainer = secondary.blend(Color.Black, 0.55f),

            tertiary = tertiary,
            onTertiary = Color.White,
            tertiaryContainer = tertiary.blend(Color.White, 0.80f),
            onTertiaryContainer = tertiary.blend(Color.Black, 0.55f)
            // surface, background, outline, error — left at lightColorScheme defaults; the
            // hybrid Material You overlay or surface tint will handle them.
        )
    }

    private fun buildDark(brand: Color, accent: Color): ColorScheme {
        val primary = brand.blend(Color.White, 0.30f)
        val secondary = accent.blend(Color.White, 0.30f)
        val tertiary = brand.blend(accent, 0.50f).blend(Color.White, 0.40f)

        return darkColorScheme(
            primary = primary,
            onPrimary = Color.Black,
            primaryContainer = brand.blend(Color.Black, 0.55f),
            onPrimaryContainer = primary.blend(Color.White, 0.55f),
            inversePrimary = brand,

            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = accent.blend(Color.Black, 0.55f),
            onSecondaryContainer = secondary.blend(Color.White, 0.55f),

            tertiary = tertiary,
            onTertiary = Color.Black,
            tertiaryContainer = tertiary.blend(Color.Black, 0.60f),
            onTertiaryContainer = tertiary.blend(Color.White, 0.45f)
        )
    }
}

internal fun Color.blend(other: Color, ratio: Float): Color {
    return Color(ColorUtils.blendARGB(this.toArgb(), other.toArgb(), ratio))
}
