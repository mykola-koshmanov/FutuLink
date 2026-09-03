package com.futulink.android.presentation.statistics

import androidx.annotation.StringRes
import com.futulink.android.domain.model.Measurement

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState
    data object Empty : StatisticsUiState
    data class Content(val measurements: List<Measurement>) : StatisticsUiState
    data class Error(@param:StringRes val messageResId: Int) : StatisticsUiState
}
