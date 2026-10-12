package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.SaveReadoutHotkeyVirtualKeyCodeUseCase

internal data class ReadoutHotkeyUseCases(
    val observeVirtualKeyCode: ObserveReadoutHotkeyVirtualKeyCodeUseCase,
    val saveVirtualKeyCode: SaveReadoutHotkeyVirtualKeyCodeUseCase,
    val observeRegistrationState: ObserveReadoutHotkeyRegistrationStateUseCase,
    val observePress: ObserveReadoutHotkeyUseCase,
)
