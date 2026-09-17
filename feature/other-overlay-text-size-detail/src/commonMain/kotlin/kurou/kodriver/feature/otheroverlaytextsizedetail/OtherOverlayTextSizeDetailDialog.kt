package kurou.kodriver.feature.otheroverlaytextsizedetail

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.OverlayTextSize
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.Res
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.overlay_text_size_cancel
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.overlay_text_size_large
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.overlay_text_size_medium
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.overlay_text_size_ok
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.overlay_text_size_small
import kurou.kodriver.feature.otheroverlaytextsizedetail.generated.resources.overlay_text_size_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherOverlayTextSizeDetail のダイアログを表示する Composable。
 */
@Composable
fun OtherOverlayTextSizeDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherOverlayTextSizeDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherOverlayTextSizeDetailDialogContent(
        uiState = uiState,
        onOverlayTextSizeSelected = viewModel::onPendingOverlayTextSizeSelected,
        onConfirm = {
            viewModel.onConfirm()
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
internal fun OtherOverlayTextSizeDetailDialogContent(
    uiState: OtherOverlayTextSizeDetailUiState,
    onOverlayTextSizeSelected: (OverlayTextSize) -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val onOverlayTextSizeSelectedWithHaptic: (OverlayTextSize) -> Unit = { overlayTextSize ->
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onOverlayTextSizeSelected(overlayTextSize)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.overlay_text_size_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(KoDriverSpacing.extraSmall))
                OverlayTextSize.entries.forEach { overlayTextSize ->
                    val label =
                        when (overlayTextSize) {
                            OverlayTextSize.SMALL -> stringResource(Res.string.overlay_text_size_small)
                            OverlayTextSize.MEDIUM -> stringResource(Res.string.overlay_text_size_medium)
                            OverlayTextSize.LARGE -> stringResource(Res.string.overlay_text_size_large)
                        }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clickable { onOverlayTextSizeSelectedWithHaptic(overlayTextSize) },
                    ) {
                        RadioButton(
                            selected = uiState.pendingOverlayTextSize == overlayTextSize,
                            onClick = { onOverlayTextSizeSelectedWithHaptic(overlayTextSize) },
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.overlay_text_size_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.overlay_text_size_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherOverlayTextSizeDetailDialogPreview() {
    OtherOverlayTextSizeDetailDialogContent(
        uiState = OtherOverlayTextSizeDetailUiState(),
    )
}
