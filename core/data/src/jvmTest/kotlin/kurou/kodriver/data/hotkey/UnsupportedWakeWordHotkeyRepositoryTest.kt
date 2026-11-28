package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsupportedWakeWordHotkeyRepositoryTest {
    @Test
    fun `押下を通知せず再試行後も非対応を返す`() =
        runTest {
            val repository = UnsupportedWakeWordHotkeyRepository()

            assertEquals(emptyList(), repository.pressStream().toList())
            repository.retryRegistration()
            assertEquals(HotkeyRegistrationState.Unsupported, repository.registrationStateStream().first())
        }
}
