package com.futulink.android.presentation.speed

import androidx.annotation.StringRes

sealed interface SpeedTestUiState {

    data object Idle : SpeedTestUiState

    data class Running(
        val currentMbps: Double,
        val elapsedMillis: Long,
        val progress: Float,
    ) : SpeedTestUiState

    data class Completed(
        val averageMbps: Double,
        val peakMbps: Double,
    ) : SpeedTestUiState

    /** See [com.futulink.android.presentation.startup.StartupUiState] for why this is a resource id. */
    data class Error(
        @param:StringRes val messageResId: Int,
    ) : SpeedTestUiState
}
