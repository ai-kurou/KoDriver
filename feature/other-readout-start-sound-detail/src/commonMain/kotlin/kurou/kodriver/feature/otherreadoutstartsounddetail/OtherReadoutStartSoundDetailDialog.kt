package kurou.kodriver.feature.otherreadoutstartsounddetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverDialogTextButton
import kurou.kodriver.core.designsystem.KoDriverRadioRow
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.domain.model.ReadoutStartSoundType
import kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources.Res
import kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_cancel
import kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_electronic_noise
import kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_formula_radio
import kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_ok
import kurou.kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * OtherReadoutStartSoundDetail のダイアログを表示する Composable。
 */
@Composable
public fun OtherReadoutStartSoundDetailDialog(
    simulatorId: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherReadoutStartSoundDetailViewModel =
        koinViewModel(key = simulatorId, parameters = { parametersOf(simulatorId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    OtherReadoutStartSoundDetailDialogContent(
        uiState = uiState,
        onTypeSelected = viewModel::onPendingTypeSelected,
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
internal fun OtherReadoutStartSoundDetailDialogContent(
    uiState: OtherReadoutStartSoundDetailUiState,
    onTypeSelected: (ReadoutStartSoundType) -> Unit = {},
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // AlertDialog の内容は別ウィンドウで組み立てられるため、外側で読んだハプティックを部品へ渡す。
    val haptic = LocalHapticFeedback.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.readout_start_sound_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(KoDriverSpacing.extraSmall))
                ReadoutStartSoundType.entries.forEach { type ->
                    val label =
                        when (type) {
                            ReadoutStartSoundType.ELECTRONIC_NOISE -> {
                                stringResource(Res.string.readout_start_sound_electronic_noise)
                            }

                            ReadoutStartSoundType.FORMULA_RADIO -> {
                                stringResource(Res.string.readout_start_sound_formula_radio)
                            }
                        }
                    KoDriverRadioRow(
                        selected = uiState.pendingType == type,
                        label = label,
                        onClick = { onTypeSelected(type) },
                        hapticFeedback = haptic,
                    )
                }
            }
        },
        confirmButton = {
            KoDriverDialogTextButton(
                text = stringResource(Res.string.readout_start_sound_ok),
                onClick = onConfirm,
                hapticFeedback = haptic,
            )
        },
        dismissButton = {
            KoDriverDialogTextButton(
                text = stringResource(Res.string.readout_start_sound_cancel),
                onClick = onDismiss,
                hapticFeedback = haptic,
            )
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherReadoutStartSoundDetailDialogPreview() {
    KoDriverTheme {
        OtherReadoutStartSoundDetailDialogContent(
            uiState = OtherReadoutStartSoundDetailUiState(),
        )
    }
}
