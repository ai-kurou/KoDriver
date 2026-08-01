@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidExitConfirmationEnabledRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidExitConfirmationEnabledRepository

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("exit_confirmation_test", ".preferences_pb")
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(testDispatcher + SupervisorJob()),
                produceFile = { tempFile },
            )
        repository = AndroidExitConfirmationEnabledRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `初期状態はtrueを返す`() =
        runTest(testDispatcher) {
            assertTrue(repository.exitConfirmationEnabled().first())
        }

    @Test
    fun `saveExitConfirmationEnabled falseの後にfalseを返す`() =
        runTest(testDispatcher) {
            repository.saveExitConfirmationEnabled(false)

            assertFalse(repository.exitConfirmationEnabled().first())
        }

    @Test
    fun `saveExitConfirmationEnabled trueで上書きするとtrueを返す`() =
        runTest(testDispatcher) {
            repository.saveExitConfirmationEnabled(false)
            repository.saveExitConfirmationEnabled(true)

            assertTrue(repository.exitConfirmationEnabled().first())
        }
}
