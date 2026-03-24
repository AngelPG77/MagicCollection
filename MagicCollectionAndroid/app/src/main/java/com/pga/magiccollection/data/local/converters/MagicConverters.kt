package com.pga.magiccollection.data.local.converters

import androidx.room.TypeConverter
import com.pga.magiccollection.domain.model.enums.CardCondition
import com.pga.magiccollection.domain.model.enums.Language

class MagicConverters {

    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromCondition(condition: CardCondition?): String? = condition?.name

    @TypeConverter
    fun toCondition(value: String?): CardCondition? {
        return value?.let { CardCondition.valueOf(it) }
    }

    @TypeConverter
    fun fromLanguage(language: Language?): String? = language?.name

    @TypeConverter
    fun toLanguage(value: String?): Language? {
        return value?.let { Language.valueOf(it) }
    }

}
