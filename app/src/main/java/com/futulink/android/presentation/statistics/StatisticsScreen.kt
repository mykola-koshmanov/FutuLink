package com.futulink.android.presentation.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.futulink.android.R
import com.futulink.android.domain.model.Measurement
import com.futulink.android.domain.model.TestMode
import com.futulink.android.presentation.components.MessageState
import com.futulink.android.presentation.components.Spacing
import com.futulink.android.presentation.components.formatMbps
import com.futulink.android.presentation.components.formatTimestamp
import com.futulink.android.ui.theme.FutuLinkTheme

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StatisticsContent(uiState = uiState, modifier = modifier)
}

@Composable
fun StatisticsContent(uiState: StatisticsUiState, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.padding(
                start = Spacing.large,
                end = Spacing.large,
                top = Spacing.large,
                bottom = Spacing.medium,
            )
        ) {
            Text(
                text = stringResource(R.string.statistics_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(R.string.statistics_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (uiState) {
            StatisticsUiState.Loading -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }

            StatisticsUiState.Empty -> MessageState(
                iconResId = R.drawable.ic_history,
                iconContentDescription = stringResource(R.string.cd_history),
                title = stringResource(R.string.statistics_empty_title),
                message = stringResource(R.string.statistics_empty_message),
            )

            is StatisticsUiState.Error -> MessageState(
                iconResId = R.drawable.ic_warning,
                iconContentDescription = stringResource(R.string.cd_error),
                title = stringResource(R.string.statistics_error_title),
                message = stringResource(R.string.statistics_error_message),
                iconTint = MaterialTheme.colorScheme.error,
                iconBackground = MaterialTheme.colorScheme.errorContainer,
            )

            is StatisticsUiState.Content -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = Spacing.large,
                    end = Spacing.large,
                    bottom = Spacing.large,
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                items(
                    items = uiState.measurements,
                    // Stable key: rows keep their identity when the newest item is prepended.
                    key = { measurement -> measurement.id },
                ) { measurement ->
                    MeasurementCard(measurement)
                }
            }
        }
    }
}

@Composable
private fun MeasurementCard(measurement: Measurement, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(Spacing.medium)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.statistics_item_type),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = formatTimestamp(measurement.createdAtEpochMillis),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacing.medium),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
            ) {
                MeasurementValue(
                    label = stringResource(R.string.speed_test_average),
                    value = measurement.averageValue,
                    unit = measurement.unit,
                    accent = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                MeasurementValue(
                    label = stringResource(R.string.speed_test_peak),
                    value = measurement.maximumValue,
                    unit = measurement.unit,
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MeasurementValue(
    label: String,
    value: Double,
    unit: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = accent,
        )
        Text(
            text = "${formatMbps(value)} $unit",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun StatisticsContentPreview() {
    FutuLinkTheme {
        StatisticsContent(
            uiState = StatisticsUiState.Content(
                measurements = listOf(
                    Measurement(
                        id = 2,
                        mode = TestMode.SPEED,
                        minimumValue = null,
                        averageValue = 92.13,
                        maximumValue = 118.77,
                        unit = "Mbps",
                        createdAtEpochMillis = 1_788_348_900_000L,
                    ),
                    Measurement(
                        id = 1,
                        mode = TestMode.SPEED,
                        minimumValue = null,
                        averageValue = 54.02,
                        maximumValue = 71.40,
                        unit = "Mbps",
                        createdAtEpochMillis = 1_788_262_500_000L,
                    ),
                )
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun StatisticsContentEmptyPreview() {
    FutuLinkTheme { StatisticsContent(uiState = StatisticsUiState.Empty) }
}
