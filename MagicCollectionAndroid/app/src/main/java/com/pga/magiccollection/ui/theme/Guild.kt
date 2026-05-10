package com.pga.magiccollection.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * The five MTG mana colors in their canonical, brand-faithful values.
 *
 * These are the same hex values Wizards of the Coast uses in the official mana symbols.
 * They are intentionally saturated/dark — UI usage derives softer tones from them via
 * [GuildScheme.scheme] rather than displaying these raw on large surfaces.
 */
object MtgManaColor {
    val White = Color(0xFFF8E7B9)   // Plains
    val Blue = Color(0xFF0E68AB)    // Island
    val Black = Color(0xFF1A1A1A)   // Swamp (lifted from pure black for contrast)
    val Red = Color(0xFFD3202A)     // Mountain
    val Green = Color(0xFF00733E)   // Forest
    val Colorless = Color(0xFFCAC5C0)
}

/**
 * The ten Ravnica guilds — every two-color combination, each with a thematic identity.
 * The display name is shown in Settings; [primary] and [secondary] are the two MTG colors
 * that compose the guild and are used by [GuildScheme] to derive the full Material color
 * scheme.
 */
enum class Guild(
    val displayName: String,
    val primary: Color,
    val secondary: Color
) {
    Azorius("Azorius", MtgManaColor.White, MtgManaColor.Blue),
    Dimir("Dimir", MtgManaColor.Blue, MtgManaColor.Black),
    Rakdos("Rakdos", MtgManaColor.Black, MtgManaColor.Red),
    Gruul("Gruul", MtgManaColor.Red, MtgManaColor.Green),
    Selesnya("Selesnya", MtgManaColor.Green, MtgManaColor.White),
    Orzhov("Orzhov", MtgManaColor.White, MtgManaColor.Black),
    Izzet("Izzet", MtgManaColor.Blue, MtgManaColor.Red),
    Golgari("Golgari", MtgManaColor.Black, MtgManaColor.Green),
    Boros("Boros", MtgManaColor.Red, MtgManaColor.White),
    Simic("Simic", MtgManaColor.Green, MtgManaColor.Blue);

    companion object {
        val Default = Azorius

        /** Tolerates legacy preference values (e.g. "Purple" from the old single-color theme). */
        fun fromPreferenceValue(value: String?): Guild {
            if (value == null) return Default
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Default
        }
    }
}
