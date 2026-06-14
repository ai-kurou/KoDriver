package kurou.kodriver.feature.otherserveripdetail

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherserveripdetail.generated.resources.Res
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_cancel
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_invalid
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_label
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_placeholder
import kodriver.feature.otherserveripdetail.generated.resources.server_ip_save
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
        onSave = {
            viewModel.onSave()
            onDismiss()
        },
        onDismiss = {
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
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.server_ip_title)) },
        text = {
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
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.isInputValid && uiState.inputIp.isNotEmpty(),
            ) {
                Text(stringResource(Res.string.server_ip_save))
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
