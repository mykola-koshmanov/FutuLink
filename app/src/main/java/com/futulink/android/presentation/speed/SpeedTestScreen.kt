package com.futulink.android.presentation.speed

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futulink.android.R
import com.futulink.android.domain.model.SpeedTestConfig
import com.futulink.android.presentation.components.Spacing
import com.futulink.android.presentation.components.formatMbps
import com.futulink.android.presentation.components.formatSeconds
import com.futulink.android.ui.theme.FutuLinkTheme

private val TEST_DURATION_SECONDS = (SpeedTestConfig.TEST_DURATION_MILLIS / 1_000L).toString()

@Composable
fun SpeedTestScreen(
    viewModel: SpeedTestViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SpeedTestContent(
        uiState = uiState,
        onStart = viewModel::startTest,
        onStop = viewModel::stopTest,
        modifier = modifier,
    )
}

/** Stateless content, so the screen can be previewed and reasoned about without a ViewModel. */
@Composable
fun SpeedTestContent(
    uiState: SpeedTestUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.large, vertical = Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.speed_test_label).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.small / 2),
        )

        SpeedGaugeCard(
            uiState = uiState,
            modifier = Modifier.padding(top = Spacing.large),
        )

        ActionButton(
            uiState = uiState,
            onStart = onStart,
            onStop = onStop,
            modifier = Modifier.padding(top = Spacing.large),
        )

        when (uiState) {
            is SpeedTestUiState.Completed -> ResultRow(
                averageMbps = uiState.averageMbps,
                peakMbps = uiState.peakMbps,
                modifier = Modifier.padding(top = Spacing.large),
            )

            is SpeedTestUiState.Error -> ErrorCard(
                messageResId = uiState.messageResId,
                modifier = Modifier.padding(top = Spacing.large),
            )

            SpeedTestUiState.Idle, is SpeedTestUiState.Running -> Unit
        }
    }
}

@Composable
private fun SpeedGaugeCard(uiState: SpeedTestUiState, modifier: Modifier = Modifier) {
    val currentMbps = when (uiState) {
        is SpeedTestUiState.Running -> uiState.currentMbps
        is SpeedTestUiState.Completed -> uiState.averageMbps
        else -> 0.0
    }
    val elapsedMillis = when (uiState) {
        is SpeedTestUiState.Running -> uiState.elapsedMillis
        // A finished test ran the whole window, so the elapsed label must agree with the bar.
        is SpeedTestUiState.Completed -> SpeedTestConfig.TEST_DURATION_MILLIS
        else -> 0L
    }
    val targetProgress = when (uiState) {
        is SpeedTestUiState.Running -> uiState.progress
        is SpeedTestUiState.Completed -> 1f
        else -> 0f
    }
    // Smooths the 500 ms sampling steps into a continuously moving bar.
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        label = "speed-test-progress",
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Status is always spelled out in words, never signalled by colour alone.
            Text(
                text = stringResource(statusLabelFor(uiState)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            // Rendered directly rather than through AnimatedContent: a cross-fade would blank
            // the number on every 500 ms sample, which hides the value it is meant to show.
            Text(
                text = formatMbps(currentMbps),
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.medium),
            )

            Text(
                text = stringResource(R.string.speed_test_unit),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.secondary,
            )

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.large)
                    .height(8.dp),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                gapSize = 0.dp,
                drawStopIndicator = {},
            )

            Text(
                text = stringResource(
                    R.string.speed_test_elapsed,
                    formatSeconds(elapsedMillis),
                    TEST_DURATION_SECONDS,
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = Spacing.small),
            )
        }
    }
}

@Composable
private fun ActionButton(
    uiState: SpeedTestUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonModifier = modifier
        .fillMaxWidth()
        .height(52.dp)

    if (uiState is SpeedTestUiState.Running) {
        OutlinedButton(
            onClick = onStop,
            modifier = buttonModifier,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            ButtonLabel(iconResId = R.drawable.ic_stop, text = stringResource(R.string.speed_test_stop))
        }
    } else {
        val labelResId = when (uiState) {
            is SpeedTestUiState.Completed -> R.string.speed_test_run_again
            is SpeedTestUiState.Error -> R.string.speed_test_try_again
            else -> R.string.speed_test_start
        }
        Button(
            onClick = onStart,
            modifier = buttonModifier,
            shape = RoundedCornerShape(14.dp),
        ) {
            ButtonLabel(iconResId = R.drawable.ic_play_arrow, text = stringResource(labelResId))
        }
    }
}

@Composable
private fun ButtonLabel(iconResId: Int, text: String) {
    Icon(
        painter = painterResource(iconResId),
        contentDescription = null,
        modifier = Modifier.size(20.dp),
    )
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier.padding(start = Spacing.small),
    )
}

@Composable
private fun ResultRow(averageMbps: Double, peakMbps: Double, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        ResultCard(
            title = stringResource(R.string.speed_test_average),
            value = formatMbps(averageMbps),
            accent = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        ResultCard(
            title = stringResource(R.string.speed_test_peak),
            value = formatMbps(peakMbps),
            accent = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ResultCard(
    title: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = accent,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = Spacing.small / 2),
            )
            Text(
                text = stringResource(R.string.speed_test_unit),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ErrorCard(messageResId: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
    ) {
        Row(
            modifier = Modifier.padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_warning),
                contentDescription = stringResource(R.string.cd_error),
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(messageResId),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(start = Spacing.medium),
            )
        }
    }
}

private fun statusLabelFor(uiState: SpeedTestUiState): Int = when (uiState) {
    SpeedTestUiState.Idle -> R.string.speed_test_status_idle
    is SpeedTestUiState.Running -> R.string.speed_test_status_running
    is SpeedTestUiState.Completed -> R.string.speed_test_status_completed
    is SpeedTestUiState.Error -> R.string.speed_test_status_error
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun SpeedTestContentIdlePreview() {
    FutuLinkTheme {
        SpeedTestContent(uiState = SpeedTestUiState.Idle, onStart = {}, onStop = {})
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun SpeedTestContentRunningPreview() {
    FutuLinkTheme {
        SpeedTestContent(
            uiState = SpeedTestUiState.Running(currentMbps = 87.42, elapsedMillis = 4_500L, progress = 0.45f),
            onStart = {},
            onStop = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun SpeedTestContentCompletedPreview() {
    FutuLinkTheme {
        SpeedTestContent(
            uiState = SpeedTestUiState.Completed(averageMbps = 92.13, peakMbps = 118.77),
            onStart = {},
            onStop = {},
        )
    }
}
