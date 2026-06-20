package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherserveripdetail.generated.resources.Res
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_cancel
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_connectivity_warning
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_description
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_invalid
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_label
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_placeholder
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_save
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_save_anyway
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherServerIpDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherServerIpDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherServerIpDetailDialogContent(
        uiState = uiState,
        onIpChanged = viewModel::onIpChanged,
        onSave = viewModel::onSave,
        onSaveAnyway = viewModel::onSaveAnyway,
        onDismiss = {
            viewModel.onDismiss()
            onDismiss()
        },
        onSaved = {
            viewModel.onDismiss()
            onDismiss()
        },
        modifier = modifier,
    )
}

@Composable
internal fun OtherServerIpDetailDialogContent(
    uiState: OtherServerIpDetailUiState,
    onIpChanged: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onSaveAnyway: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onSaved: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onSaved()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.server_ip_title)) },
        text = {
            Column {
                Text(stringResource(Res.string.server_ip_description))
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = uiState.inputIp,
                    onValueChange = onIpChanged,
                    label = { Text(stringResource(Res.string.server_ip_label)) },
                    placeholder = { Text(stringResource(Res.string.server_ip_placeholder)) },
                    isError = !uiState.isInputValid,
                    supportingText = if (!uiState.isInputValid) {
                        { Text(stringResource(Res.string.server_ip_invalid)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.connectivityWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(Res.string.server_ip_connectivity_warning),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            if (uiState.connectivityWarning) {
                TextButton(onClick = onSaveAnyway) {
                    Text(stringResource(Res.string.server_ip_save_anyway))
                }
            } else {
                TextButton(
                    onClick = onSave,
                    enabled = uiState.isInputValid && uiState.inputIp.isNotEmpty() && !uiState.isCheckingConnectivity,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isCheckingConnectivity) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(Res.string.server_ip_save))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.server_ip_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailDialogPreview() {
    OtherServerIpDetailDialogContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100"),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailDialogInvalidPreview() {
    OtherServerIpDetailDialogContent(
        uiState = OtherServerIpDetailUiState(inputIp = "invalid", isInputValid = false),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailDialogConnectivityWarningPreview() {
    OtherServerIpDetailDialogContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100", connectivityWarning = true),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherServerIpDetailDialogCheckingConnectivityPreview() {
    OtherServerIpDetailDialogContent(
        uiState = OtherServerIpDetailUiState(inputIp = "192.168.1.100", isCheckingConnectivity = true),
    )
}
