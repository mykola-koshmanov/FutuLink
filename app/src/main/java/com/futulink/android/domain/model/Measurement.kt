package com.futulink.android.domain.model

/**
 * One successfully completed measurement.
 *
 * The shape is deliberately generic (minimum/average/maximum plus a unit) so a future
 * Ping test can be stored in the same table. Speed measurements have no meaningful
 * minimum, so [minimumValue] is null for them.
 */
data class Measurement(
    val id: Long,
    val mode: TestMode,
    val minimumValue: Double?,
    val averageValue: Double,
    val maximumValue: Double,
    val unit: String,
    val createdAtEpochMillis: Long,
)
