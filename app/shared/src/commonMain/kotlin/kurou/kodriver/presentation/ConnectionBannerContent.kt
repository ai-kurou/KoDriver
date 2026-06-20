package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

@Composable
fun ConnectionBanner(
    uiState: ConnectionBannerUiState,
    modifier: Modifier = Modifier,
) {
    val backgroundColor = when (uiState.status) {
        ConnectionBannerStatus.CONNECTED -> MaterialTheme.colorScheme.secondaryContainer
        ConnectionBannerStatus.UNCHECKED, ConnectionBannerStatus.DISCONNECTED ->
            MaterialTheme.colorScheme.errorContainer
    }
    val contentColor = when (uiState.status) {
        ConnectionBannerStatus.CONNECTED -> MaterialTheme.colorScheme.onSecondaryContainer
        ConnectionBannerStatus.UNCHECKED, ConnectionBannerStatus.DISCONNECTED ->
            MaterialTheme.colorScheme.onErrorContainer
    }
    val icon = when (uiState.iconType) {
        ConnectionBannerIconType.NETWORK ->
            if (uiState.isConnected) Icons.Default.Wifi else Icons.Default.WifiOff
        ConnectionBannerIconType.SIMULATOR ->
            if (uiState.isConnected) Icons.Default.SportsScore else Icons.Default.PowerOff
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = uiState.message,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
        )
    }
}

private class ConnectionBannerPreviewProvider : PreviewParameterProvider<ConnectionBannerUiState> {
    override val values = sequenceOf(
        ConnectionBannerUiState(
            status = ConnectionBannerStatus.CONNECTED,
            message = "シミュレータに接続中",
            iconType = ConnectionBannerIconType.SIMULATOR,
        ),
        ConnectionBannerUiState(
            status = ConnectionBannerStatus.DISCONNECTED,
            message = "シミュレータ接続待機中",
            iconType = ConnectionBannerIconType.SIMULATOR,
        ),
        ConnectionBannerUiState(
            status = ConnectionBannerStatus.CONNECTED,
            message = "Windows版KoDriverに接続中",
            iconType = ConnectionBannerIconType.NETWORK,
        ),
        ConnectionBannerUiState(
            status = ConnectionBannerStatus.DISCONNECTED,
            message = "Windows版KoDriver接続待機中",
            iconType = ConnectionBannerIconType.NETWORK,
        ),
        ConnectionBannerUiState(
            status = ConnectionBannerStatus.DISCONNECTED,
            message = "接続先IPアドレスが未設定です",
            iconType = ConnectionBannerIconType.NETWORK,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ConnectionBannerPreview(
    @PreviewParameter(ConnectionBannerPreviewProvider::class) uiState: ConnectionBannerUiState,
) {
    KoDriverTheme {
        ConnectionBanner(uiState = uiState)
    }
}
