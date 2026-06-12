package kurou.kodriver.presentation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview

internal enum class ConnectionStatus {
    Hidden,
    Waiting,
    Connected,
}

@Composable
internal fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    when (status) {
        ConnectionStatus.Hidden -> Unit
        ConnectionStatus.Waiting -> LinearProgressIndicator(
            modifier = modifier
                .fillMaxWidth()
                .testTag(CONNECTION_STATUS_TEST_TAG),
            color = MaterialTheme.colorScheme.secondary,
        )
        ConnectionStatus.Connected -> LinearProgressIndicator(
            progress = { 1f },
            modifier = modifier
                .fillMaxWidth()
                .testTag(CONNECTION_STATUS_TEST_TAG),
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

internal const val CONNECTION_STATUS_TEST_TAG = "connection_status"

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusIndicatorWaitingPreview() {
    KoDriverTheme {
        ConnectionStatusIndicator(status = ConnectionStatus.Waiting)
    }
}

@Preview(showBackground = true)
@Composable
private fun ConnectionStatusIndicatorConnectedPreview() {
    KoDriverTheme {
        ConnectionStatusIndicator(status = ConnectionStatus.Connected)
    }
}
