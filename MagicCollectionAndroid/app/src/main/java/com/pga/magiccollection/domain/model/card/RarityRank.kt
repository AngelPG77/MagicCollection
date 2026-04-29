package com.pga.magiccollection.domain.model.card

object RarityRank {
    const val COMMON = 0
    const val UNCOMMON = 1
    const val RARE = 2
    const val MYTHIC = 3

    fun fromCode(rarity: String?): Int {
        return when (rarity?.trim()?.lowercase()) {
            "common" -> COMMON
            "uncommon" -> UNCOMMON
            "rare" -> RARE
            "mythic" -> MYTHIC
            else -> COMMON
        }
    }
}
