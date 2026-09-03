package com.futulink.android.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.futulink.android.domain.model.TestMode
import com.futulink.android.presentation.speed.SpeedTestScreen
import com.futulink.android.presentation.speed.SpeedTestViewModel
import com.futulink.android.presentation.statistics.StatisticsScreen
import com.futulink.android.presentation.statistics.StatisticsViewModel
import com.futulink.android.presentation.unavailable.ModeUnavailableScreen
import org.koin.androidx.compose.koinViewModel

/**
 * Main application shell: a bottom bar with the Test and Statistics destinations.
 *
 * The SpeedTestViewModel is resolved here rather than inside [SpeedTestScreen] so that leaving
 * the Test tab can cancel a running measurement *before* the Test content leaves composition.
 * Doing it with a DisposableEffect inside the Test screen would also fire on a configuration
 * change, which must not cancel anything.
 */
@Composable
fun MainScreen(
    mode: TestMode,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.TEST) }
    val speedTestViewModel: SpeedTestViewModel = koinViewModel()
    val statisticsViewModel: StatisticsViewModel = koinViewModel()

    CancelMeasurementWhenNotVisible(speedTestViewModel)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                MainTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = {
                            if (tab == selectedTab) return@NavigationBarItem
                            if (selectedTab == MainTab.TEST) {
                                speedTestViewModel.cancelRunningTest()
                            }
                            selectedTab = tab
                        },
                        icon = {
                            Icon(
                                painter = painterResource(tab.iconResId),
                                contentDescription = null,
                            )
                        },
                        label = { Text(text = stringResource(tab.labelResId)) },
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)

        when (selectedTab) {
            MainTab.TEST -> when (mode) {
                TestMode.SPEED -> SpeedTestScreen(
                    viewModel = speedTestViewModel,
                    modifier = contentModifier,
                )

                TestMode.PING -> ModeUnavailableScreen(modifier = contentModifier)
            }

            MainTab.STATISTICS -> StatisticsScreen(
                viewModel = statisticsViewModel,
                modifier = contentModifier,
            )
        }
    }
}

/**
 * A measurement belongs to the visible session, so pressing Home, switching apps or locking the
 * device stops it instead of downloading in the background.
 *
 * ON_STOP also fires while the Activity is recreated for a configuration change, which must *not*
 * cancel anything — `Activity.isChangingConfigurations` is what tells the two cases apart.
 */
@Composable
private fun CancelMeasurementWhenNotVisible(viewModel: SpeedTestViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val activity = LocalActivity.current

    DisposableEffect(lifecycleOwner, activity) {
        val observer = LifecycleEventObserver { _, event ->
            val isRecreating = activity?.isChangingConfigurations == true
            if (event == Lifecycle.Event.ON_STOP && !isRecreating) {
                viewModel.cancelRunningTest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}
