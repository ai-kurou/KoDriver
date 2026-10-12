package kurou.kodriver.core.windowshotkeydata

import kurou.kodriver.domain.model.HotkeyRegistrationState

sealed interface GlobalHotkeyEvent {
    data class Registration(
        val state: HotkeyRegistrationState,
    ) : GlobalHotkeyEvent

    data object Pressed : GlobalHotkeyEvent
}
