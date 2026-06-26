package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

private data class BannerColors(val background: Color, val content: Color)

@Composable
private fun bannerColors(status: ConnectionBannerStatus): BannerColors = when (status) {
    ConnectionBannerStatus.CONNECTED -> BannerColors(
        background = MaterialTheme.colorScheme.secondaryContainer,
        content = MaterialTheme.colorScheme.onSecondaryContainer,
    )
    ConnectionBannerStatus.DISCONNECTED -> BannerColors(
        background = Color(0xFFFFF9C4),
        content = Color(0xFF5F4B00),
    )
    ConnectionBannerStatus.UNCHECKED -> BannerColors(
        background = MaterialTheme.colorScheme.errorContainer,
        content = MaterialTheme.colorScheme.onErrorContainer,
    )
}

private fun bannerIcon(iconType: ConnectionBannerIconType, isConnected: Boolean): ImageVector =
    when (iconType) {
        ConnectionBannerIconType.NETWORK ->
            if (isConnected) Icons.Default.Wifi else Icons.Default.WifiOff
        ConnectionBannerIconType.SIMULATOR ->
            if (isConnected) Icons.Default.SportsScore else Icons.Default.PowerOff
    }

@Composable
fun ConnectionBannerContent(
    uiState: ConnectionBannerUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val colors = bannerColors(uiState.status)
    val icon = bannerIcon(uiState.iconType, uiState.isConnected)
    val isTappable = uiState.isTappable && onClick != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .then(
                if (isTappable) {
                    Modifier.clickable(role = Role.Button, onClick = onClick!!)
                } else {
                    Modifier
                },
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = uiState.message,
                style = MaterialTheme.typography.labelMedium,
                color = colors.content,
            )
        }
        if (isTappable) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                contentDescription = null,
                tint = colors.content,
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.CenterEnd),
            )
        }
    }
}

private class ConnectionBannerContentPreviewParameterProvider : PreviewParameterProvider<ConnectionBannerUiState> {
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
            status = ConnectionBannerStatus.UNCHECKED,
            message = "接続先IPアドレスが未設定です",
            iconType = ConnectionBannerIconType.NETWORK,
            isTappable = true,
            tapNavigationItemId = "console_ip",
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun ConnectionBannerContentPreview(
    @PreviewParameter(ConnectionBannerContentPreviewParameterProvider::class) uiState: ConnectionBannerUiState,
) {
    ConnectionBannerContent(uiState = uiState, onClick = {})
}
