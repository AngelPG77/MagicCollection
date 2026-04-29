package com.pga.magiccollection.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(tokenizer = FtsOptions.TOKENIZER_UNICODE61, notIndexed = ["language"])
@Entity(tableName = "card_names_fts")
data class CardNameFtsEntity(
    @ColumnInfo(name = "card_id")
    val cardId: String,
    val name: String,
    val language: String
)
