package kurou.kodriver.data.hotkey

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsupportedHotkeyRepositoryTest {
    @Test
    fun `非対応環境では再試行しても非対応のままで押下は通知しない`() =
        runTest {
            val repository = UnsupportedHotkeyRepository()
            repository.retryRegistration()
            assertEquals(HotkeyRegistrationState.Unsupported, repository.registrationStateStream().first())
            assertEquals(emptyList(), repository.hotkeyPressStream().toList())
        }
}
