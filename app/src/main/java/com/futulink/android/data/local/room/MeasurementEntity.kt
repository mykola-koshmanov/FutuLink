package com.futulink.android.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mode: String,
    val minimumValue: Double?,
    val averageValue: Double,
    val maximumValue: Double,
    val unit: String,
    val createdAtEpochMillis: Long,
)
