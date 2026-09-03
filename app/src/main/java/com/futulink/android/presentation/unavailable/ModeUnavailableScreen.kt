package com.futulink.android.presentation.unavailable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.futulink.android.R
import com.futulink.android.presentation.components.MessageState
import com.futulink.android.ui.theme.FutuLinkTheme

/** Shown when the remote configuration selects PING, which this version does not implement. */
@Composable
fun ModeUnavailableScreen(modifier: Modifier = Modifier) {
    MessageState(
        iconResId = R.drawable.ic_info,
        iconContentDescription = stringResource(R.string.cd_information),
        title = stringResource(R.string.mode_unavailable_title),
        message = stringResource(R.string.mode_unavailable_message),
        modifier = modifier,
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF4F6FA)
@Composable
private fun ModeUnavailableScreenPreview() {
    FutuLinkTheme { ModeUnavailableScreen() }
}
