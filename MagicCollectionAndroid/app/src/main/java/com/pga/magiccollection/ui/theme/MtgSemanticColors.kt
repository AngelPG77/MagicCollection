package com.pga.magiccollection.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic color tokens that don't depend on the active guild.
 *
 * MTG-specific concepts (mana symbols, rarity, foil) must always look the same regardless
 * of which guild the user picked — the mana symbols are sacred. The active guild only
 * influences `primary`/`secondary`/`tertiary` in the [androidx.compose.material3.ColorScheme],
 * never these tokens.
 *
 * Access via [LocalMtgSemanticColors].
 */
@Immutable
data class MtgSemanticColors(
    // Canonical mana colors, brand-faithful
    val manaWhite: Color,
    val manaBlue: Color,
    val manaBlack: Color,
    val manaRed: Color,
    val manaGreen: Color,
    val manaColorless: Color,

    // Rarities, modeled after the official MTG rarity gem palette
    val rarityCommon: Color,
    val rarityUncommon: Color,
    val rarityRare: Color,
    val rarityMythic: Color,

    // Status — needed in inventory/sync flows where Material's `error` is too aggressive
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,

    // Foil shimmer (used for foil-card chips)
    val foilStart: Color,
    val foilEnd: Color
)

private val LightSemanticColors = MtgSemanticColors(
    manaWhite = MtgManaColor.White,
    manaBlue = MtgManaColor.Blue,
    manaBlack = MtgManaColor.Black,
    manaRed = MtgManaColor.Red,
    manaGreen = MtgManaColor.Green,
    manaColorless = MtgManaColor.Colorless,
    rarityCommon = Color(0xFF1F1F1F),
    rarityUncommon = Color(0xFF7C8B97),
    rarityRare = Color(0xFFB8A668),
    rarityMythic = Color(0xFFD55A2C),
    success = Color(0xFF1B7A3E),
    onSuccess = Color(0xFFFFFFFF),
    warning = Color(0xFFB36300),
    onWarning = Color(0xFFFFFFFF),
    foilStart = Color(0xFFB37BD9),
    foilEnd = Color(0xFF6BD4D9)
)

private val DarkSemanticColors = MtgSemanticColors(
    manaWhite = MtgManaColor.White,
    manaBlue = Color(0xFF4A9BD9),
    manaBlack = Color(0xFF8C8C8C),
    manaRed = Color(0xFFE54B55),
    manaGreen = Color(0xFF39A06A),
    manaColorless = MtgManaColor.Colorless,
    rarityCommon = Color(0xFFD9D9D9),
    rarityUncommon = Color(0xFFB7C7D5),
    rarityRare = Color(0xFFE5D08F),
    rarityMythic = Color(0xFFFF8552),
    success = Color(0xFF6FCF97),
    onSuccess = Color(0xFF002B12),
    warning = Color(0xFFFFB74D),
    onWarning = Color(0xFF3A2300),
    foilStart = Color(0xFFD4AAFA),
    foilEnd = Color(0xFF93EAEF)
)

fun semanticColorsFor(isDark: Boolean): MtgSemanticColors =
    if (isDark) DarkSemanticColors else LightSemanticColors

val LocalMtgSemanticColors = compositionLocalOf<MtgSemanticColors> {
    error("MtgSemanticColors not provided. Wrap composables in MagicCollectionAppTheme.")
}
