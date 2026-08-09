package kurou.kodriver.feature.telemetryloglist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.feature.telemetryloglist.generated.resources.Res
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_delete_confirm_body
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_delete_confirm_button
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_delete_confirm_title
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_reset_cancel_button
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TelemetryLogDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.telemetry_log_delete_confirm_title)) },
        text = { Text(stringResource(Res.string.telemetry_log_delete_confirm_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(Res.string.telemetry_log_delete_confirm_button),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.telemetry_log_reset_cancel_button))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun TelemetryLogDeleteConfirmDialogPreview() {
    TelemetryLogDeleteConfirmDialog(
        onConfirm = {},
        onDismiss = {},
    )
}
