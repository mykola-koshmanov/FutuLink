package com.futulink.android.domain.model

/** Everything a running speed test reports to the presentation layer. */
sealed interface SpeedTestUpdate {

    /** Emitted once per sampling interval while the test is running. */
    data class Progress(
        val currentMbps: Double,
        val elapsedMillis: Long,
        val progress: Float,
    ) : SpeedTestUpdate

    /** Emitted once, after the measurement window ended and the download stopped. */
    data class Completed(val result: SpeedTestResult) : SpeedTestUpdate
}
