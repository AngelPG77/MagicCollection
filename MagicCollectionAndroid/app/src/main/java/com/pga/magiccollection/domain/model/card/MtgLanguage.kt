package com.pga.magiccollection.domain.model.card

enum class MtgLanguage(val code: String, val displayName: String, val estimatedSizeMb: Float) {
    ENGLISH("en", "English", 2.5f),
    SPANISH("es", "Spanish", 2.8f),
    FRENCH("fr", "Français", 2.7f),
    GERMAN("de", "Deutsch", 2.6f),
    ITALIAN("it", "Italiano", 2.6f),
    PORTUGUESE("pt", "Português", 2.6f),
    JAPANESE("ja", "日本語", 3.2f),
    KOREAN("ko", "한국어", 3.0f),
    RUSSIAN("ru", "Русский", 3.1f),
    CHINESE_SIMPLIFIED("zhs", "汉语 (Simplified)", 3.3f),
    CHINESE_TRADITIONAL("zht", "漢語 (Traditional)", 3.4f);

    companion object {
        fun fromCode(code: String) = entries.find { it.code == code } ?: ENGLISH
    }
}
