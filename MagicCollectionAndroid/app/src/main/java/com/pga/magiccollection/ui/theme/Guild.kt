package com.pga.magiccollection.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.pga.magiccollection.R

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

    /** Alias kept for callers that ask for `guild.guildName` rather than `displayName`. */
    val guildName: String get() = displayName

    /**
     * Hybrid mana symbol drawable for this guild. File names follow the canonical MTG
     * "color1color2" abbreviation — e.g. Azorius (W+U) → `ic_hybrid_wu`. The lowercase
     * order is the conventional one used by the WoTC style guide.
     */
    @get:DrawableRes
    val hybridIconRes: Int get() = when (this) {
        Azorius -> R.drawable.ic_hybrid_wu
        Dimir -> R.drawable.ic_hybrid_ub
        Rakdos -> R.drawable.ic_hybrid_br
        Gruul -> R.drawable.ic_hybrid_rg
        Selesnya -> R.drawable.ic_hybrid_gw
        Orzhov -> R.drawable.ic_hybrid_wb
        Izzet -> R.drawable.ic_hybrid_ur
        Golgari -> R.drawable.ic_hybrid_bg
        Boros -> R.drawable.ic_hybrid_rw
        Simic -> R.drawable.ic_hybrid_gu
    }

    /**
     * A more saturated/dark version of the guild colors, intended for text on light
     * surfaces. Computed lazily from the primary so each guild "reads" well at high
     * contrast without redefining the value table.
     */
    val readable: Color get() = when (this) {
        Azorius -> Color(0xFF003D5B)
        Dimir -> Color(0xFF0A2233)
        Rakdos -> Color(0xFF8B0000)
        Gruul -> Color(0xFF004422)
        Selesnya -> Color(0xFF004422)
        Orzhov -> Color(0xFF1A1718)
        Izzet -> Color(0xFF003D5B)
        Golgari -> Color(0xFF004422)
        Boros -> Color(0xFF8B0000)
        Simic -> Color(0xFF003D5B)
    }

    companion object {
        val Default = Azorius

        /** Tolerates legacy preference values (e.g. "Purple" from the old single-color theme). */
        fun fromPreferenceValue(value: String?): Guild {
            if (value == null) return Default
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Default
        }

        /** Same as [fromPreferenceValue] but with a non-null contract. */
        fun fromName(name: String): Guild = fromPreferenceValue(name)

        /**
         * Resolves a guild from a card's color identity (e.g. ["W","U"] → Azorius).
         * Returns null when the input doesn't match an exact two-color guild — callers
         * decide whether to fall back to a default theme.
         */
        fun fromColors(colors: List<String>): Guild? {
            if (colors.size != 2) return null
            val c = colors.map { it.uppercase() }.toSet()
            return when {
                c == setOf("W", "U") -> Azorius
                c == setOf("U", "B") -> Dimir
                c == setOf("B", "R") -> Rakdos
                c == setOf("R", "G") -> Gruul
                c == setOf("G", "W") -> Selesnya
                c == setOf("W", "B") -> Orzhov
                c == setOf("U", "R") -> Izzet
                c == setOf("B", "G") -> Golgari
                c == setOf("R", "W") -> Boros
                c == setOf("G", "U") -> Simic
                else -> null
            }
        }
    }
}
