package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse

class JvmExitConfirmationPreferencesRepositoryTest {

    private val repository = JvmExitConfirmationPreferencesRepository()

    @Test
    fun `exitConfirmationEnabledはfalseを返す`() = runTest {
        assertFalse(repository.exitConfirmationEnabled().first())
    }

    @Test
    fun `saveExitConfirmationEnabledを呼び出してもfalseを返す`() = runTest {
        repository.saveExitConfirmationEnabled(true)

        assertFalse(repository.exitConfirmationEnabled().first())
    }
}
