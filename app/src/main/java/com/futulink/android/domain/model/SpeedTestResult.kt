package com.futulink.android.domain.model

/** Final numbers of a speed test that ran for the full measurement window. */
data class SpeedTestResult(
    val averageMbps: Double,
    val peakMbps: Double,
)
