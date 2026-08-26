package kurou.kodriver.data.device

import kotlin.test.Test
import kotlin.test.assertFalse

class JvmHapticFeedbackAvailabilityRepositoryTest {
    private val repository = JvmHapticFeedbackAvailabilityRepository()

    @Test
    fun `isHapticFeedbackAvailableはfalseを返す`() {
        assertFalse(repository.isHapticFeedbackAvailable())
    }
}
