package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT

enum class HotkeyCheckState {
    Idle,
    Waiting,
    Detected,
    TimedOut,
}

data class ReadoutHotkeyUiState(
    val virtualKeyCode: Int = LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT,
    val registrationState: HotkeyRegistrationState = HotkeyRegistrationState.Unsupported,
    val checkState: HotkeyCheckState = HotkeyCheckState.Idle,
)
