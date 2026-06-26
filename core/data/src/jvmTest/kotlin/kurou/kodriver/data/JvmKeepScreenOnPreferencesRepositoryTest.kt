package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

class JvmKeepScreenOnPreferencesRepositoryTest {

    private val repository = JvmKeepScreenOnPreferencesRepository()

    @Test
    fun `keepScreenOnはfalseを返す`() = runTest {
        assertFalse(repository.keepScreenOn().first())
    }

    @Test
    fun `saveKeepScreenOnを呼び出してもfalseを返す`() = runTest {
        repository.saveKeepScreenOn(true)

        assertFalse(repository.keepScreenOn().first())
    }
}
