@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

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
class AndroidHapticFeedbackEnabledRepositoryTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidHapticFeedbackEnabledRepository

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("haptic_feedback_enabled_test", ".preferences_pb")
        val dataStore =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(testDispatcher + SupervisorJob()),
                produceFile = { tempFile },
            )
        repository = AndroidHapticFeedbackEnabledRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `初期状態はtrueを返す`() =
        runTest(testDispatcher) {
            assertTrue(repository.hapticFeedbackEnabled().first())
        }

    @Test
    fun `saveHapticFeedbackEnabled falseの後にfalseを返す`() =
        runTest(testDispatcher) {
            repository.saveHapticFeedbackEnabled(false)

            assertFalse(repository.hapticFeedbackEnabled().first())
        }

    @Test
    fun `saveHapticFeedbackEnabled trueで上書きするとtrueを返す`() =
        runTest(testDispatcher) {
            repository.saveHapticFeedbackEnabled(false)
            repository.saveHapticFeedbackEnabled(true)

            assertTrue(repository.hapticFeedbackEnabled().first())
        }
}
