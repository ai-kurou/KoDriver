package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.feature.otherserveripdetail.generated.resources.Res
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_discovery_dialog_cancel
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_discovery_dialog_confirm
import kurou.kodriver.feature.otherserveripdetail.generated.resources.server_ip_discovery_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun OtherServerIpDiscoveryDialog(
    discoveredServers: List<DiscoveredServer>,
    selectedDiscoveredServer: DiscoveredServer?,
    onServerSelected: (DiscoveredServer) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val onServerSelectedWithHaptic: (DiscoveredServer) -> Unit = { server ->
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onServerSelected(server)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.server_ip_discovery_dialog_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                discoveredServers.forEach { server ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onServerSelectedWithHaptic(server) },
                    ) {
                        RadioButton(
                            selected = selectedDiscoveredServer == server,
                            onClick = { onServerSelectedWithHaptic(server) },
                        )
                        Text("${server.hostName} (${server.ipAddress})")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.server_ip_discovery_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.server_ip_discovery_dialog_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherServerIpDiscoveryDialogPreview() {
    val servers =
        listOf(
            DiscoveredServer(hostName = "DESKTOP-ABC123", ipAddress = "192.168.1.10"),
            DiscoveredServer(hostName = "DESKTOP-XYZ999", ipAddress = "192.168.1.20"),
        )
    OtherServerIpDiscoveryDialog(
        discoveredServers = servers,
        selectedDiscoveredServer = servers.first(),
        onServerSelected = {},
        onConfirm = {},
        onDismiss = {},
    )
}
