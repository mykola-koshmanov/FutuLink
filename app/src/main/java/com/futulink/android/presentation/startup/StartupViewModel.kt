package com.futulink.android.presentation.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.futulink.android.R
import com.futulink.android.domain.usecase.ResolveStartupModeUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StartupViewModel(
    private val resolveStartupMode: ResolveStartupModeUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<StartupUiState>(StartupUiState.Loading)
    val uiState: StateFlow<StartupUiState> = _uiState.asStateFlow()

    private var resolveJob: Job? = null

    init {
        // Started from the ViewModel, not from composition: a recomposition or a rotation
        // must never trigger a second config request.
        resolveMode()
    }

    fun retry() {
        resolveMode()
    }

    private fun resolveMode() {
        // Ignore Retry taps while a request is already in flight.
        if (resolveJob?.isActive == true) return

        _uiState.value = StartupUiState.Loading
        resolveJob = viewModelScope.launch {
            try {
                _uiState.value = StartupUiState.Ready(resolveStartupMode())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = StartupUiState.Error(R.string.startup_error_message)
            }
        }
    }
}
