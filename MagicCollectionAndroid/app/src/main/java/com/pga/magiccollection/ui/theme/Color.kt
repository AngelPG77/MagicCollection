package com.pga.magiccollection.ui.theme

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
