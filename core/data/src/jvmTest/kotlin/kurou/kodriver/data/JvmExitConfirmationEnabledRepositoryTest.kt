package kurou.kodriver.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class JvmExitConfirmationEnabledRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_exit_confirmation_pref_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はtrueを返す`() = testScope.runTest {
        val repository = createExitConfirmationEnabledRepository(tempDir.absolutePath)

        assertTrue(repository.exitConfirmationEnabled().first())
    }

    @Test
    fun `falseを保存するとfalseを返す`() = testScope.runTest {
        val repository = createExitConfirmationEnabledRepository(tempDir.absolutePath)

        repository.saveExitConfirmationEnabled(false)

        assertFalse(repository.exitConfirmationEnabled().first())
    }

    @Test
    fun `trueを保存するとtrueを返す`() = testScope.runTest {
        val repository = createExitConfirmationEnabledRepository(tempDir.absolutePath)
        repository.saveExitConfirmationEnabled(false)

        repository.saveExitConfirmationEnabled(true)

        assertTrue(repository.exitConfirmationEnabled().first())
    }

    @Test
    fun `DataStoreがIOエラーを起こした場合はデフォルト値trueを返す`() = testScope.runTest {
        val repository = JvmExitConfirmationEnabledRepository(BrokenDataStore())

        assertTrue(repository.exitConfirmationEnabled().first())
    }
}

private class BrokenDataStore : DataStore<Preferences> {
    override val data: Flow<Preferences> = flow { throw java.io.IOException("simulated IO error") }
    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences =
        throw java.io.IOException("simulated IO error")
}
