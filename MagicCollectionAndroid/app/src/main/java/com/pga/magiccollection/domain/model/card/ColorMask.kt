package com.pga.magiccollection.domain.model.card

object ColorMask {
    const val WHITE = 1      // 2^0
    const val BLUE = 2       // 2^1
    const val BLACK = 4      // 2^2
    const val RED = 8        // 2^3
    const val GREEN = 16     // 2^4
    const val COLORLESS = 32 // 2^5

    private val symbolToBit = mapOf(
        "W" to WHITE,
        "U" to BLUE,
        "B" to BLACK,
        "R" to RED,
        "G" to GREEN,
        "C" to COLORLESS
    )

    fun fromSymbols(symbols: Collection<String>?): Int {
        if (symbols.isNullOrEmpty()) return 0
        return symbols.fold(0) { acc, symbol ->
            val normalized = symbol.trim().uppercase()
            acc or normalized.fold(0) { symbolAcc, char ->
                symbolAcc or (symbolToBit[char.toString()] ?: 0)
            }
        }
    }

    fun fromSymbolsOrMana(symbols: Collection<String>?, manaCost: String?): Int {
        val fromSymbols = fromSymbols(symbols)
        return if (fromSymbols != 0) fromSymbols else fromManaCost(manaCost)
    }

    fun fromManaCost(manaCost: String?): Int {
        if (manaCost.isNullOrBlank()) return 0

        val tokenRegex = "\\{([^}]*)\\}".toRegex()
        return tokenRegex.findAll(manaCost.uppercase())
            .fold(0) { acc, match ->
                val token = match.groupValues[1]
                acc or token.fold(0) { tokenAcc, char ->
                    tokenAcc or (symbolToBit[char.toString()] ?: 0)
                }
            }
    }
}
