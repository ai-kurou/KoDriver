package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

class JvmDynamicColorEnabledRepositoryTest {

    private val repository = JvmDynamicColorEnabledRepository()

    @Test
    fun `dynamicColorEnabledはfalseを返す`() = runTest {
        assertFalse(repository.dynamicColorEnabled().first())
    }

    @Test
    fun `saveDynamicColorEnabledを呼び出してもfalseを返す`() = runTest {
        repository.saveDynamicColorEnabled(true)

        assertFalse(repository.dynamicColorEnabled().first())
    }
}
