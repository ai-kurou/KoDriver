package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.testTag
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_check
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_conflict
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_detected
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_failed
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_key
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_prompt
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_registered
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_registering
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_timed_out
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_title
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.readout_hotkey_waiting
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ReadoutHotkeyCard(
    state: ReadoutHotkeyUiState,
    onKeySelected: (Int) -> Unit,
    onCheck: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    DetailPaneCard(
        title = stringResource(Res.string.readout_hotkey_title),
        modifier = modifier.testTag("readout_hotkey_card"),
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                Text(readoutHotkeyDisplayName(state.virtualKeyCode))
                OutlinedTextField(
                    value =
                        if (focused) {
                            stringResource(Res.string.readout_hotkey_prompt)
                        } else {
                            readoutHotkeyDisplayName(state.virtualKeyCode)
                        },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(Res.string.readout_hotkey_key)) },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag("readout_hotkey_key")
                            .onFocusChanged { focused = it.isFocused }
                            .onPreviewKeyEvent { event ->
                                val key = readoutHotkeyVirtualKeyCode(event.key)
                                if (event.type == KeyEventType.KeyDown && key != null) {
                                    onKeySelected(key)
                                    true
                                } else {
                                    false
                                }
                            },
                )
                when (val registration = state.registrationState) {
                    HotkeyRegistrationState.Unsupported -> {}

                    HotkeyRegistrationState.Registering -> {
                        Text(stringResource(Res.string.readout_hotkey_registering))
                    }

                    HotkeyRegistrationState.Registered -> {
                        Text(stringResource(Res.string.readout_hotkey_registered))
                    }

                    HotkeyRegistrationState.Conflict -> {
                        Text(stringResource(Res.string.readout_hotkey_conflict))
                    }

                    is HotkeyRegistrationState.Failed -> {
                        Text(stringResource(Res.string.readout_hotkey_failed, registration.errorCode))
                    }
                }
                Button(
                    onClick = onCheck,
                    enabled =
                        state.registrationState == HotkeyRegistrationState.Registered &&
                            state.checkState != HotkeyCheckState.Waiting,
                ) {
                    Text(stringResource(Res.string.readout_hotkey_check))
                }
                when (state.checkState) {
                    HotkeyCheckState.Idle -> Unit
                    HotkeyCheckState.Waiting -> Text(stringResource(Res.string.readout_hotkey_waiting))
                    HotkeyCheckState.Detected -> Text(stringResource(Res.string.readout_hotkey_detected))
                    HotkeyCheckState.TimedOut -> Text(stringResource(Res.string.readout_hotkey_timed_out))
                }
            }
        },
    )
}
