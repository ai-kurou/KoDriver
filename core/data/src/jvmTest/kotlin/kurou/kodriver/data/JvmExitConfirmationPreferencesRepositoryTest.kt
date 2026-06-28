package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JvmExitConfirmationPreferencesRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_exit_confirmation_pref_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はtrueを返す`() = testScope.runTest {
        val repository = createExitConfirmationPreferencesRepository(tempDir.absolutePath)

        assertTrue(repository.exitConfirmationEnabled().first())
    }

    @Test
    fun `falseを保存するとfalseを返す`() = testScope.runTest {
        val repository = createExitConfirmationPreferencesRepository(tempDir.absolutePath)

        repository.saveExitConfirmationEnabled(false)

        assertFalse(repository.exitConfirmationEnabled().first())
    }

    @Test
    fun `trueを保存するとtrueを返す`() = testScope.runTest {
        val repository = createExitConfirmationPreferencesRepository(tempDir.absolutePath)
        repository.saveExitConfirmationEnabled(false)

        repository.saveExitConfirmationEnabled(true)

        assertTrue(repository.exitConfirmationEnabled().first())
    }
}
