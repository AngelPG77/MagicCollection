package com.pga.magiccollection.domain.model.enums

enum class Language(val code: String, val displayName: String) {
    ENGLISH("EN", "English"),
    SPANISH("ES", "Spanish"),
    FRENCH("FR", "French"),
    GERMAN("DE", "German"),
    ITALIAN("IT", "Italian"),
    PORTUGUESE("PT", "Portuguese"),
    JAPANESE("JP", "Japanese"),
    CHINESE("CN", "Chinese"),
    RUSSIAN("RU", "Russian"),
    KOREAN("KR", "Korean");

    companion object {
        fun fromCode(text: String?): Language {
            if (text == null) throw IllegalArgumentException("Language cannot be empty")
            return values().find {
                it.code.equals(text, ignoreCase = true) ||
                        it.name.equals(text, ignoreCase = true) ||
                        it.displayName.equals(text, ignoreCase = true)
            } ?: throw IllegalArgumentException("Unsupported language: $text")
        }
    }
}