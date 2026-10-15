package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsupportedManualReadoutHotkeyRepositoryTest {
    @Test
    fun `押下を通知せず再試行後も非対応を返す`() =
        runTest {
            val repository = UnsupportedManualReadoutHotkeyRepository()
            assertEquals(emptyList(), repository.pressStream().toList())
            repository.retryRegistration("lmu_windows", "set-1")
            assertEquals(
                HotkeyRegistrationState.Unsupported,
                repository.registrationStateStream("lmu_windows", "set-1").first(),
            )
        }
}
