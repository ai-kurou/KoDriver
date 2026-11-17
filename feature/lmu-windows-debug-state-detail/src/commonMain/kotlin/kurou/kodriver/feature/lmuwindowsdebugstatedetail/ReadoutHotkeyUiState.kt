package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT

public enum class HotkeyCheckState {
    Idle,
    Waiting,
    Detected,
    TimedOut,
}

public data class ReadoutHotkeyUiState(
    val virtualKeyCode: Int = LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT,
    val registrationState: HotkeyRegistrationState = HotkeyRegistrationState.Unsupported,
    val checkState: HotkeyCheckState = HotkeyCheckState.Idle,
)
