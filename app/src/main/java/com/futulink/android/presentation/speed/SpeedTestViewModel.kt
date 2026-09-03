package com.futulink.android.presentation.speed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futulink.android.R
import com.futulink.android.domain.model.SpeedTestException
import com.futulink.android.domain.model.SpeedTestFailureReason
import com.futulink.android.domain.model.SpeedTestUpdate
import com.futulink.android.domain.repository.SpeedTestRepository
import com.futulink.android.domain.usecase.SaveMeasurementUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SpeedTestViewModel(
    private val speedTestRepository: SpeedTestRepository,
    private val saveMeasurement: SaveMeasurementUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<SpeedTestUiState>(SpeedTestUiState.Idle)
    val uiState: StateFlow<SpeedTestUiState> = _uiState.asStateFlow()

    /** At most one measurement runs at a time; this handle is the only way to cancel it. */
    private var measurementJob: Job? = null

    fun startTest() {
        // A second tap while a test is running is ignored instead of starting a parallel download.
        if (measurementJob?.isActive == true) return

        // Job.cancel() only *requests* cancellation; the previous measurement may still be
        // unwinding (closing its response, releasing the connection). Keeping the handle and
        // joining it first means a fast Stop -> Start can never leave two downloads overlapping.
        val previousMeasurement = measurementJob

        _uiState.value = SpeedTestUiState.Running(currentMbps = 0.0, elapsedMillis = 0L, progress = 0f)
        measurementJob = viewModelScope.launch {
            previousMeasurement?.join()
            try {
                speedTestRepository.runSpeedTest().collect { update -> handleUpdate(update) }
            } catch (exception: CancellationException) {
                // Stop, tab switch or a cleared ViewModel: the state is handled by the caller
                // and cancellation must keep propagating so the download really stops.
                throw exception
            } catch (exception: SpeedTestException) {
                _uiState.value = SpeedTestUiState.Error(messageResIdFor(exception.reason))
            } catch (exception: Exception) {
                // Anything else (for example a failed database insert) is reported the same way,
                // without leaking the technical detail into the UI.
                _uiState.value = SpeedTestUiState.Error(R.string.speed_test_error_save)
            }
        }
    }

    /**
     * Cancels the running measurement and returns to the idle state. The job handle is kept, not
     * cleared, so the next [startTest] can wait for this cancellation to finish.
     */
    fun stopTest() {
        measurementJob?.cancel()
        _uiState.value = SpeedTestUiState.Idle
    }

    /**
     * Called when the Test tab is left and when the app stops being visible. A finished or failed
     * test keeps its result on screen, only a running measurement is cancelled.
     */
    fun cancelRunningTest() {
        if (measurementJob?.isActive == true) stopTest()
    }

    private suspend fun handleUpdate(update: SpeedTestUpdate) {
        when (update) {
            is SpeedTestUpdate.Progress -> {
                _uiState.value = SpeedTestUiState.Running(
                    currentMbps = update.currentMbps,
                    elapsedMillis = update.elapsedMillis,
                    progress = update.progress,
                )
            }

            is SpeedTestUpdate.Completed -> {
                // Save first: the result is only presented as completed once it is in history.
                saveMeasurement(update.result)
                _uiState.value = SpeedTestUiState.Completed(
                    averageMbps = update.result.averageMbps,
                    peakMbps = update.result.peakMbps,
                )
            }
        }
    }

    private fun messageResIdFor(reason: SpeedTestFailureReason): Int = when (reason) {
        SpeedTestFailureReason.NETWORK -> R.string.speed_test_error_network
        SpeedTestFailureReason.NO_DATA -> R.string.speed_test_error_no_data
    }
}
