package com.futulink.android.presentation.startup

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.futulink.android.R
import com.futulink.android.presentation.components.FutuLinkLogo
import com.futulink.android.presentation.components.MessageState
import com.futulink.android.presentation.components.Spacing
import com.futulink.android.ui.theme.FutuLinkTheme

/** Branded loading screen shown while the very first configuration request runs. */
@Composable
fun StartupLoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        FutuLinkLogo()

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = Spacing.large),
        )

        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = Spacing.extraLarge)
                .size(32.dp),
            strokeWidth = 3.dp,
        )

        Text(
            text = stringResource(R.string.startup_loading_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.medium),
        )
    }
}

/** Full error screen for a failed first configuration request. */
@Composable
fun StartupErrorScreen(
    @StringRes messageResId: Int,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MessageState(
        iconResId = R.drawable.ic_warning,
        iconContentDescription = stringResource(R.string.cd_error),
        title = stringResource(R.string.startup_error_title),
        message = stringResource(messageResId),
        iconTint = MaterialTheme.colorScheme.error,
        iconBackground = MaterialTheme.colorScheme.errorContainer,
        modifier = modifier,
    ) {
        Button(onClick = onRetry) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = stringResource(R.string.startup_retry),
                modifier = Modifier.padding(start = Spacing.small),
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun StartupLoadingScreenPreview() {
    FutuLinkTheme { StartupLoadingScreen() }
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun StartupErrorScreenPreview() {
    FutuLinkTheme {
        StartupErrorScreen(
            messageResId = R.string.startup_error_message,
            onRetry = {},
        )
    }
}
