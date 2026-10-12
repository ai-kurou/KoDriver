package kurou.kodriver.feature.otheroverlaytextcolordetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.OVERLAY_TEXT_COLOR_DEFAULT
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.Res
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_cancel
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_ok
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_reset
import kurou.kodriver.feature.otheroverlaytextcolordetail.generated.resources.overlay_text_color_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * OtherOverlayTextColorDetail のダイアログを表示する Composable。
 */
@Composable
fun OtherOverlayTextColorDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherOverlayTextColorDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherOverlayTextColorDetailDialogContent(
        uiState = uiState,
        onOverlayTextColorSelected = viewModel::onPendingOverlayTextColorSelected,
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
internal fun OtherOverlayTextColorDetailDialogContent(
    uiState: OtherOverlayTextColorDetailUiState,
    onOverlayTextColorSelected: (Long) -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val onOverlayTextColorSelectedWithHaptic: (Long) -> Unit = { overlayTextColor ->
        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
        onOverlayTextColorSelected(overlayTextColor)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.overlay_text_color_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()).selectableGroup()) {
                Spacer(modifier = Modifier.height(KoDriverSpacing.extraSmall))
                OverlayTextColorPreset.entries.forEach { preset ->
                    val label = stringResource(preset.label)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = uiState.pendingOverlayTextColor == preset.argb,
                                    role = Role.RadioButton,
                                    onClick = { onOverlayTextColorSelectedWithHaptic(preset.argb) },
                                ).semantics { contentDescription = label },
                    ) {
                        RadioButton(
                            selected = uiState.pendingOverlayTextColor == preset.argb,
                            onClick = null,
                        )
                        Box(modifier = Modifier.size(24.dp).background(Color(preset.argb)))
                        Text(label)
                    }
                }
                TextButton(onClick = { onOverlayTextColorSelectedWithHaptic(OVERLAY_TEXT_COLOR_DEFAULT) }) {
                    Text(stringResource(Res.string.overlay_text_color_reset))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.overlay_text_color_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.overlay_text_color_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherOverlayTextColorDetailDialogPreview() {
    KoDriverTheme {
        OtherOverlayTextColorDetailDialogContent(
            uiState = OtherOverlayTextColorDetailUiState(),
        )
    }
}
