package com.pga.magiccollection.domain.model.enums

enum class Language(val code: String, val displayName: String) {
    ENGLISH("EN", "Inglés"),
    SPANISH("ES", "Español"),
    FRENCH("FR", "Francés"),
    GERMAN("DE", "Alemán"),
    ITALIAN("IT", "Italiano"),
    PORTUGUESE("PT", "Portugués"),
    JAPANESE("JP", "Japonés"),
    CHINESE("CN", "Chino"),
    RUSSIAN("RU", "Ruso"),
    KOREAN("KR", "Coreano");

    companion object {
        fun fromCode(text: String?): Language {
            if (text == null) throw IllegalArgumentException("El idioma no puede estar vacío")
            return values().find {
                it.code.equals(text, ignoreCase = true) ||
                        it.name.equals(text, ignoreCase = true) ||
                        it.displayName.equals(text, ignoreCase = true)
            } ?: throw IllegalArgumentException("Idioma no soportado: $text")
        }
    }
}