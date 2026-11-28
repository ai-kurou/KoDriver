package kurou.kodriver.feature.otherwakewordhotkeydetail

import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers

internal data class OtherWakeWordHotkeyDetailUiState(
    val hotkeyVirtualKeyCode: Int? = null,
    val hotkeyModifiers: ManualReadoutHotkeyModifiers = MANUAL_READOUT_HOTKEY_MODIFIERS_DEFAULT,
    val hotkeyConflict: OtherWakeWordHotkeyConflict? = null,
    val hotkeyRegistrationState: HotkeyRegistrationState = HotkeyRegistrationState.Unsupported,
    val speechRecognitionEnabled: Boolean = false,
) {
    /** 割り当て済みでも、音声認識が OFF の間はホットキーに反応しないことを案内する。 */
    val showSpeechRecognitionOffNotice: Boolean
        get() = hotkeyVirtualKeyCode != null && !speechRecognitionEnabled
}
