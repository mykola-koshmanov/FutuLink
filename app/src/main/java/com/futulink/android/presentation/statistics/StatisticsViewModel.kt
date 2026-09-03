package com.futulink.android.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futulink.android.R
import com.futulink.android.domain.repository.MeasurementRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val STOP_TIMEOUT_MILLIS = 5_000L

class StatisticsViewModel(
    measurementRepository: MeasurementRepository,
) : ViewModel() {

    /**
     * Room drives this flow: an inserted measurement re-emits the list and the screen updates
     * on its own. Flow.catch is transparent to cancellation, so leaving the screen is not an error.
     */
    val uiState: StateFlow<StatisticsUiState> = measurementRepository.observeMeasurements()
        .map { measurements ->
            if (measurements.isEmpty()) {
                StatisticsUiState.Empty
            } else {
                StatisticsUiState.Content(measurements)
            }
        }
        .catch { emit(StatisticsUiState.Error(R.string.statistics_error_message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = StatisticsUiState.Loading,
        )
}
