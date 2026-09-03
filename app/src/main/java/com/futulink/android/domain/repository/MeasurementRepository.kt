package com.futulink.android.domain.repository

import com.futulink.android.domain.model.Measurement
import kotlinx.coroutines.flow.Flow

interface MeasurementRepository {

    /** Stored measurements, newest first. Emits again whenever the history changes. */
    fun observeMeasurements(): Flow<List<Measurement>>

    suspend fun saveMeasurement(measurement: Measurement)
}
