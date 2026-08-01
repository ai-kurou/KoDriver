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
class AndroidDynamicColorEnabledRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidDynamicColorEnabledRepository

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("dynamic_color_enabled_test", ".preferences_pb")
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(testDispatcher + SupervisorJob()),
                produceFile = { tempFile },
            )
        repository = AndroidDynamicColorEnabledRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `初期状態はfalseを返す`() =
        runTest(testDispatcher) {
            assertFalse(repository.dynamicColorEnabled().first())
        }

    @Test
    fun `saveDynamicColorEnabled trueの後にtrueを返す`() =
        runTest(testDispatcher) {
            repository.saveDynamicColorEnabled(true)

            assertTrue(repository.dynamicColorEnabled().first())
        }

    @Test
    fun `saveDynamicColorEnabled falseで上書きするとfalseを返す`() =
        runTest(testDispatcher) {
            repository.saveDynamicColorEnabled(true)
            repository.saveDynamicColorEnabled(false)

            assertFalse(repository.dynamicColorEnabled().first())
        }
}
