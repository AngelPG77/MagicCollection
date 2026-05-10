package com.pga.magiccollection.ui.theme

import androidx.compose.ui.graphics.Color

// Brand and semantic colors live in:
//  - Guild.kt            → MtgManaColor (canonical W/U/B/R/G hex values)
//  - GuildSchemes.kt     → derived ColorScheme per guild (primary/secondary/tertiary)
//  - MtgSemanticColors.kt → mana, rarity, success/warning, foil tokens
//
// Top-level aliases used across screens (kept for compatibility with code that imports
// them directly without going through CompositionLocal). Prefer
// LocalMtgSemanticColors.current.manaWhite at new call sites — these aliases will follow.

val ManaWhite = MtgManaColor.White
val ManaBlue = MtgManaColor.Blue
val ManaBlack = MtgManaColor.Black
val ManaRed = MtgManaColor.Red
val ManaGreen = MtgManaColor.Green
val ManaColorless = MtgManaColor.Colorless

// --- Authentic MTG card frame palettes (used by HybridComponents and similar) ---
val ManaWhiteAuthentic = Color(0xFFF8F6D8)
val ManaBlueAuthentic = Color(0xFFC1D7E9)
val ManaBlackAuthentic = Color(0xFFBAB1AB)
val ManaRedAuthentic = Color(0xFFE49977)
val ManaGreenAuthentic = Color(0xFFA3C095)

// --- Vibrant variants (for UI elements that need to POP) ---
val ManaWhiteVibrant = Color(0xFFF9FAF4)
val ManaBlueVibrant = Color(0xFF0073BB)
val ManaBlackVibrant = Color(0xFF1A1718)
val ManaRedVibrant = Color(0xFFD3202A)
val ManaGreenVibrant = Color(0xFF00733E)
val ManaGoldVibrant = Color(0xFFC5B239)

// --- Readability variants (optimized for text on light/onSurface) ---
val ManaWhiteReadable = Color(0xFF5D5D30)
val ManaBlueReadable = Color(0xFF003D5B)
val ManaBlackReadable = Color(0xFF1A1718)
val ManaRedReadable = Color(0xFF8B0000)
val ManaGreenReadable = Color(0xFF004422)
val ManaGoldReadable = Color(0xFF7A6B1A)

// --- Per-guild raw color refs (kept for legacy callers that picked individual values
// instead of using the Guild enum / GuildSchemes). New code should use GuildSchemes.of() ---
val AzoriusPrimary = ManaBlueVibrant
val AzoriusSecondary = ManaWhiteAuthentic
val DimirPrimary = Color(0xFF003D5B)
val DimirSecondary = ManaBlueAuthentic
val RakdosPrimary = ManaRedVibrant
val RakdosSecondary = ManaBlackAuthentic
val GruulPrimary = ManaGreenVibrant
val GruulSecondary = ManaRedAuthentic
val SelesnyaPrimary = Color(0xFF8A8A00)
val SelesnyaSecondary = ManaWhiteAuthentic
val OrzhovPrimary = ManaBlackVibrant
val OrzhovSecondary = ManaWhiteAuthentic
val IzzetPrimary = ManaBlueVibrant
val IzzetSecondary = ManaRedAuthentic
val GolgariPrimary = ManaGreenVibrant
val GolgariSecondary = ManaBlackAuthentic
val BorosPrimary = ManaRedVibrant
val BorosSecondary = ManaWhiteAuthentic
val SimicPrimary = ManaBlueVibrant
val SimicSecondary = ManaGreenAuthentic

// --- Misc ---
val ManaGold = Color(0xFFC5B239)
val ManaWhitePrimary = Color(0xFF626200)
val ManaBluePrimary = Color(0xFF006399)
val ManaBlackPrimary = Color(0xFF121212)
val ManaRedPrimary = Color(0xFFC00011)
val ManaGreenPrimary = Color(0xFF006D39)
val ManaColorlessPrimary = Color(0xFF605E5F)

val OffBlack = Color(0xFF151515)
val OffWhite = Color(0xFFFCFCF8)
