package com.futulink.android.data.mapper

import com.futulink.android.data.local.room.MeasurementEntity
import com.futulink.android.domain.model.Measurement
import com.futulink.android.domain.model.TestMode

/**
 * The database stores the mode as a lowercase string ("speed") instead of the enum name so
 * the table stays readable and independent from Kotlin naming.
 */
fun MeasurementEntity.toDomain(): Measurement = Measurement(
    id = id,
    mode = TestMode.fromRawValue(mode),
    minimumValue = minimumValue,
    averageValue = averageValue,
    maximumValue = maximumValue,
    unit = unit,
    createdAtEpochMillis = createdAtEpochMillis,
)

fun Measurement.toEntity(): MeasurementEntity = MeasurementEntity(
    id = id,
    mode = mode.name.lowercase(),
    minimumValue = minimumValue,
    averageValue = averageValue,
    maximumValue = maximumValue,
    unit = unit,
    createdAtEpochMillis = createdAtEpochMillis,
)
