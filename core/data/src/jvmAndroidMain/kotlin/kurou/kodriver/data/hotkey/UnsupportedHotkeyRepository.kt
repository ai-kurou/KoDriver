package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.HotkeyRepository
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository

internal class UnsupportedHotkeyRepository(
    private val preferences: ReadoutHotkeyPreferencesRepository,
) : HotkeyRepository {
    override fun observeVirtualKeyCode(): Flow<Int> = preferences.observeVirtualKeyCode()

    override suspend fun saveVirtualKeyCode(virtualKeyCode: Int) = preferences.saveVirtualKeyCode(virtualKeyCode)

    override fun registrationStateStream(): Flow<HotkeyRegistrationState> = flowOf(HotkeyRegistrationState.Unsupported)

    override fun hotkeyPressStream(): Flow<Unit> = emptyFlow()
}
