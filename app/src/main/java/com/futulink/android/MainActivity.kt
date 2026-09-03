package com.futulink.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futulink.android.presentation.main.MainScreen
import com.futulink.android.presentation.startup.StartupErrorScreen
import com.futulink.android.presentation.startup.StartupLoadingScreen
import com.futulink.android.presentation.startup.StartupUiState
import com.futulink.android.presentation.startup.StartupViewModel
import com.futulink.android.ui.theme.FutuLinkTheme
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The app is light-only, so the system bars always use dark icons on a light background.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(TRANSPARENT, TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(TRANSPARENT, TRANSPARENT),
        )
        setContent {
            FutuLinkTheme {
                FutuLinkApp()
            }
        }
    }

    private companion object {
        const val TRANSPARENT = android.graphics.Color.TRANSPARENT
    }
}

/** Startup gate: the main UI is only reachable once a test mode has been resolved. */
@Composable
private fun FutuLinkApp() {
    val startupViewModel: StartupViewModel = koinViewModel()
    val startupState by startupViewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val state = startupState) {
            StartupUiState.Loading -> StartupLoadingScreen()

            is StartupUiState.Error -> StartupErrorScreen(
                messageResId = state.messageResId,
                onRetry = startupViewModel::retry,
            )

            is StartupUiState.Ready -> MainScreen(mode = state.mode)
        }
    }
}
