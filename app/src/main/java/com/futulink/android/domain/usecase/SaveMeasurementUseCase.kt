package com.futulink.android.domain.usecase

import com.futulink.android.domain.model.Measurement
import com.futulink.android.domain.model.SpeedTestResult
import com.futulink.android.domain.model.TestMode
import com.futulink.android.domain.repository.MeasurementRepository

/** Turns a finished speed test into a history record. Only called for successful tests. */
class SaveMeasurementUseCase(private val measurementRepository: MeasurementRepository) {

    suspend operator fun invoke(result: SpeedTestResult) {
        val measurement = Measurement(
            id = NEW_MEASUREMENT_ID,
            mode = TestMode.SPEED,
            // A download test has no meaningful minimum value; a future Ping test would fill it.
            minimumValue = null,
            averageValue = result.averageMbps,
            maximumValue = result.peakMbps,
            unit = MBPS_UNIT,
            // Wall-clock time is correct here: this timestamp is displayed, not measured with.
            createdAtEpochMillis = System.currentTimeMillis(),
        )
        measurementRepository.saveMeasurement(measurement)
    }

    private companion object {
        /** Room replaces this with the generated primary key. */
        const val NEW_MEASUREMENT_ID = 0L
        const val MBPS_UNIT = "Mbps"
    }
}
