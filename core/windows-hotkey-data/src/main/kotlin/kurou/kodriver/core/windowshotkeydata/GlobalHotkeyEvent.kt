package kurou.kodriver.core.windowshotkeydata

import kurou.kodriver.domain.manualreadout.model.HotkeyRegistrationState

internal sealed interface GlobalHotkeyEvent {
    data class Registration(
        val state: HotkeyRegistrationState,
    ) : GlobalHotkeyEvent

    data object Pressed : GlobalHotkeyEvent
}
