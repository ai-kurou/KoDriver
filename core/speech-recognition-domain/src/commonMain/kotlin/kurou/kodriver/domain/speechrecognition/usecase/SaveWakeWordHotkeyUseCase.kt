package kurou.kodriver.domain.speechrecognition.usecase

import kurou.kodriver.domain.model.isHotkeyVirtualKeyCode
import kurou.kodriver.domain.speechrecognition.model.WakeWordHotkey
import kurou.kodriver.domain.speechrecognition.repository.WakeWordHotkeyPreferencesRepository

public class SaveWakeWordHotkeyUseCase(
    private val repository: WakeWordHotkeyPreferencesRepository,
) {
    public suspend operator fun invoke(hotkey: WakeWordHotkey) {
        val code = hotkey.virtualKeyCode
        require(code == null || isHotkeyVirtualKeyCode(code)) { "Unsupported virtual key code: $code" }
        repository.saveWakeWordHotkey(hotkey)
    }
}
