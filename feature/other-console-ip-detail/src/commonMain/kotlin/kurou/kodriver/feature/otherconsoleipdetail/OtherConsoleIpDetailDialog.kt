package kurou.kodriver.feature.otherconsoleipdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherconsoleipdetail.generated.resources.Res
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_cancel
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_description
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_invalid
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_label
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_placeholder
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_save
import kodriver.feature.otherconsoleipdetail.generated.resources.console_ip_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherConsoleIpDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherConsoleIpDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherConsoleIpDetailDialogContent(
        uiState = uiState,
        onAddressChanged = viewModel::onAddressChanged,
        onSave = viewModel::onSave,
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
internal fun OtherConsoleIpDetailDialogContent(
    uiState: OtherConsoleIpDetailUiState,
    onAddressChanged: (String) -> Unit = {},
    onSave: () -> Unit = {},
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
        title = { Text(stringResource(Res.string.console_ip_title)) },
        text = {
            Column {
                Text(stringResource(Res.string.console_ip_description))
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = uiState.inputAddress,
                    onValueChange = onAddressChanged,
                    label = { Text(stringResource(Res.string.console_ip_label)) },
                    placeholder = { Text(stringResource(Res.string.console_ip_placeholder)) },
                    isError = !uiState.isInputValid,
                    supportingText = if (!uiState.isInputValid) {
                        { Text(stringResource(Res.string.console_ip_invalid)) }
                    } else {
                        null
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (uiState.saveFailed) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "保存に失敗しました",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = uiState.isInputValid && uiState.inputAddress.isNotEmpty(),
            ) {
                Text(stringResource(Res.string.console_ip_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.console_ip_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherConsoleIpDetailDialogPreview() {
    OtherConsoleIpDetailDialogContent(
        uiState = OtherConsoleIpDetailUiState(inputAddress = "192.168.1.100"),
    )
}

@Preview(showBackground = true)
@Composable
private fun OtherConsoleIpDetailDialogInvalidPreview() {
    OtherConsoleIpDetailDialogContent(
        uiState = OtherConsoleIpDetailUiState(inputAddress = "invalid", isInputValid = false),
    )
}
