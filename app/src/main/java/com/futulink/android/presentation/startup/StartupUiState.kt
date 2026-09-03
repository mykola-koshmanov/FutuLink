package com.futulink.android.presentation.startup

import androidx.annotation.StringRes
import com.futulink.android.domain.model.TestMode

/**
 * Error carries a string resource instead of a ready-made sentence: the ViewModel decides
 * *which* message applies, the UI decides how to render it. That keeps every user-facing
 * string in strings.xml and keeps the ViewModel testable without an Android Context.
 */
sealed interface StartupUiState {
    data object Loading : StartupUiState
    data class Ready(val mode: TestMode) : StartupUiState
    data class Error(@param:StringRes val messageResId: Int) : StartupUiState
}
