package com.pga.magiccollection.domain.model.enums

enum class CardCondition(val displayName: String) {
    MINT("Mint"),
    NEAR_MINT("Near Mint"),
    EXCELLENT("Excellent"),
    GOOD("Good"),
    LIGHTLY_PLAYED("Lightly Played"),
    PLAYED("Played"),
    POOR("Poor");

    companion object {
        fun fromString(condition: String?): CardCondition {
            if (condition == null) throw IllegalArgumentException("La condición no puede ser nula")
            return CardCondition.entries.find {
                it.displayName.equals(condition, ignoreCase = true) ||
                        it.name.equals(condition, ignoreCase = true)
            } ?: throw IllegalArgumentException("Condición no válida: $condition")
        }
    }
}