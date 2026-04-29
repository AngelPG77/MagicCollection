package com.pga.magiccollection.data.remote.dto

data class IndexVersionDto(
    val lastUpdated: String,
    val totalRows: Long,
    val estimatedSizeMb: Float,
    val version: String? = null,
    val checksum: String? = null
)
