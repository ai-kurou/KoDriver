package kurou.kodriver.feature.otherreadoutstartsounddetail

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.otherreadoutstartsounddetail.generated.resources.Res
import kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_cancel
import kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_electronic_noise
import kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_formula_radio
import kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_ok
import kodriver.feature.otherreadoutstartsounddetail.generated.resources.readout_start_sound_title
import kurou.kodriver.domain.model.ReadoutStartSoundType
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OtherReadoutStartSoundDetailDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: OtherReadoutStartSoundDetailViewModel = koinViewModel()
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.readout_start_sound_title)) },
        text = {
            Column {
                Spacer(modifier = Modifier.height(4.dp))
                ReadoutStartSoundType.entries.forEach { type ->
                    val label = when (type) {
                        ReadoutStartSoundType.ELECTRONIC_NOISE ->
                            stringResource(Res.string.readout_start_sound_electronic_noise)
                        ReadoutStartSoundType.FORMULA_RADIO ->
                            stringResource(Res.string.readout_start_sound_formula_radio)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTypeSelected(type) },
                    ) {
                        RadioButton(
                            selected = uiState.pendingType == type,
                            onClick = { onTypeSelected(type) },
                        )
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.readout_start_sound_ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.readout_start_sound_cancel))
            }
        },
        modifier = modifier,
    )
}

// AlertDialog はポップアップウィンドウとして別の描画ルートで描画されるため、
// Compose Multiplatform の Res リソース配列の初期化が引き継がれずプレビューが動作しない。
@Preview(showBackground = true)
@Composable
private fun OtherReadoutStartSoundDetailDialogPreview() {
    OtherReadoutStartSoundDetailDialogContent(
        uiState = OtherReadoutStartSoundDetailUiState(),
    )
}
