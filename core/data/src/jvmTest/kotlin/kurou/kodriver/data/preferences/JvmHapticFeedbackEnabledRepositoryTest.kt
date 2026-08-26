package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

class JvmHapticFeedbackEnabledRepositoryTest {
    private val repository = JvmHapticFeedbackEnabledRepository()

    @Test
    fun `hapticFeedbackEnabledはfalseを返す`() =
        runTest {
            assertFalse(repository.hapticFeedbackEnabled().first())
        }

    @Test
    fun `saveHapticFeedbackEnabledを呼び出してもfalseを返す`() =
        runTest {
            repository.saveHapticFeedbackEnabled(true)

            assertFalse(repository.hapticFeedbackEnabled().first())
        }
}
