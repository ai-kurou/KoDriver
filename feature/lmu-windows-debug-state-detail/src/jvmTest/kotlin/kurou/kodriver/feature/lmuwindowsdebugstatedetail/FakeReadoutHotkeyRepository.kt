package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyRegistrationStateUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyUseCase
import kurou.kodriver.domain.usecase.ObserveReadoutHotkeyVirtualKeyCodeUseCase
import kurou.kodriver.domain.usecase.SaveReadoutHotkeyVirtualKeyCodeUseCase

internal class FakeReadoutHotkeyRepository : HotkeyRepository {
    val key = MutableStateFlow(LMU_WINDOWS_READOUT_HOTKEY_VIRTUAL_KEY_CODE_DEFAULT)
    val registration = MutableStateFlow<HotkeyRegistrationState>(HotkeyRegistrationState.Unsupported)
    val presses = MutableSharedFlow<Unit>()

    override fun observeVirtualKeyCode(): Flow<Int> = key

    override suspend fun saveVirtualKeyCode(virtualKeyCode: Int) {
        key.update { virtualKeyCode }
    }

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = registration

    override fun hotkeyPressStream(): Flow<Unit> = presses

    fun useCases() =
        ReadoutHotkeyUseCases(
            ObserveReadoutHotkeyVirtualKeyCodeUseCase(this),
            SaveReadoutHotkeyVirtualKeyCodeUseCase(this),
            ObserveReadoutHotkeyRegistrationStateUseCase(this),
            ObserveReadoutHotkeyUseCase(this),
        )
}
