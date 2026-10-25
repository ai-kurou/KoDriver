package kurou.kodriver.feature.otherlist

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.KoDriverDestructiveConfirmDialog
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.feature.otherlist.generated.resources.Res
import kurou.kodriver.feature.otherlist.generated.resources.telemetry_log_reset_cancel_button
import kurou.kodriver.feature.otherlist.generated.resources.telemetry_log_reset_confirm_body
import kurou.kodriver.feature.otherlist.generated.resources.telemetry_log_reset_confirm_button
import kurou.kodriver.feature.otherlist.generated.resources.telemetry_log_reset_confirm_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun TelemetryLogResetConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    KoDriverDestructiveConfirmDialog(
        title = stringResource(Res.string.telemetry_log_reset_confirm_title),
        body = stringResource(Res.string.telemetry_log_reset_confirm_body),
        confirmLabel = stringResource(Res.string.telemetry_log_reset_confirm_button),
        dismissLabel = stringResource(Res.string.telemetry_log_reset_cancel_button),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun TelemetryLogResetConfirmDialogPreview() {
    KoDriverTheme {
        TelemetryLogResetConfirmDialog(
            onConfirm = {},
            onDismiss = {},
        )
    }
}
