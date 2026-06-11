package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

internal enum class ConnectionStatus {
    Waiting,
    Connected,
}

@Composable
internal fun ConnectionStatusIndicator(
    status: ConnectionStatus,
    modifier: Modifier = Modifier,
) {
    when (status) {
        ConnectionStatus.Waiting -> CircularProgressIndicator(
            modifier = modifier
                .size(16.dp)
                .testTag("connection_status"),
            color = MaterialTheme.colorScheme.secondary,
            strokeWidth = 2.5.dp,
        )
        ConnectionStatus.Connected -> Box(
            modifier = modifier
                .size(14.dp)
                .background(
                    color = MaterialTheme.colorScheme.secondary,
                    shape = CircleShape,
                )
                .testTag("connection_status"),
        )
    }
}

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
