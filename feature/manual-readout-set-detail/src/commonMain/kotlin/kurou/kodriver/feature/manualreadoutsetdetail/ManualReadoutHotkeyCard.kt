package kurou.kodriver.feature.manualreadoutsetdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_hotkey_clear
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_hotkey_debug
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_hotkey_hint
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_hotkey_none
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_hotkey_other_set
import kurou.kodriver.feature.manualreadoutsetdetail.generated.resources.manual_readout_hotkey_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualReadoutHotkeyCard(
    state: ManualReadoutSetDetailUiState,
    onAssign: (Int) -> Unit,
    onClear: () -> Unit,
) {
    DetailPaneCard(
        title = stringResource(Res.string.manual_readout_hotkey_title),
        modifier = Modifier.testTag("manual_readout_hotkey_card"),
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                OutlinedTextField(
                    value =
                        state.hotkeyVirtualKeyCode?.let { manualReadoutHotkeyDisplayName(it) }
                            ?: stringResource(Res.string.manual_readout_hotkey_none),
                    onValueChange = {},
                    readOnly = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("manual_readout_hotkey_key")
                            .onPreviewKeyEvent { event ->
                                val code = manualReadoutHotkeyVirtualKeyCode(event.key)
                                if (event.type == KeyEventType.KeyDown && code != null) {
                                    onAssign(code)
                                    true
                                } else {
                                    false
                                }
                            },
                )
                val conflictText =
                    when (val conflict = state.hotkeyConflict) {
                        is ManualReadoutHotkeyConflict.OtherSet -> {
                            stringResource(
                                Res.string.manual_readout_hotkey_other_set,
                                simulatorDisplayName(conflict.simulatorId),
                                conflict.setNumber,
                            )
                        }

                        ManualReadoutHotkeyConflict.DebugHotkey -> {
                            stringResource(Res.string.manual_readout_hotkey_debug)
                        }

                        null -> {
                            null
                        }
                    }
                if (conflictText != null) Text(conflictText, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onClear, enabled = state.hotkeyVirtualKeyCode != null) {
                    Text(stringResource(Res.string.manual_readout_hotkey_clear))
                }
                Text(stringResource(Res.string.manual_readout_hotkey_hint))
            }
        },
    )
}
