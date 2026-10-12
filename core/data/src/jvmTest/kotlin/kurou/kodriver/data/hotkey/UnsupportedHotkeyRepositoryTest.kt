package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.repository.ReadoutHotkeyPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsupportedHotkeyRepositoryTest {
    private val preferences = FakeReadoutHotkeyPreferencesRepository(0x70)

    @Test
    fun `非対応環境でも設定は保存でき押下は通知しない`() =
        runTest {
            val repository = UnsupportedHotkeyRepository(preferences)
            assertEquals(0x70, repository.observeVirtualKeyCode().first())
            repository.saveVirtualKeyCode(0x41)
            assertEquals(0x41, repository.observeVirtualKeyCode().first())
            assertEquals(HotkeyRegistrationState.Unsupported, repository.registrationStateStream().first())
            assertEquals(emptyList(), repository.hotkeyPressStream().toList())
        }
}

private class FakeReadoutHotkeyPreferencesRepository(
    initial: Int,
) : ReadoutHotkeyPreferencesRepository {
    private val state = MutableStateFlow(initial)

    override fun observeVirtualKeyCode(): Flow<Int> = state

    override suspend fun saveVirtualKeyCode(virtualKeyCode: Int) {
        state.value = virtualKeyCode
    }
}
