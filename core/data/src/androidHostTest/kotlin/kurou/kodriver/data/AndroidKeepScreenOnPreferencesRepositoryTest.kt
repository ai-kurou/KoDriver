@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidKeepScreenOnPreferencesRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidKeepScreenOnPreferencesRepository

    @Before
    fun setUp() {
        tempFile = File.createTempFile("keep_screen_on_test", ".preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
            produceFile = { tempFile },
        )
        repository = AndroidKeepScreenOnPreferencesRepository(dataStore)
    }

    @After
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `初期状態はfalseを返す`() = runTest(testDispatcher) {
        assertFalse(repository.keepScreenOn().first())
    }

    @Test
    fun `saveKeepScreenOn trueの後にtrueを返す`() = runTest(testDispatcher) {
        repository.saveKeepScreenOn(true)

        assertTrue(repository.keepScreenOn().first())
    }

    @Test
    fun `saveKeepScreenOn falseで上書きするとfalseを返す`() = runTest(testDispatcher) {
        repository.saveKeepScreenOn(true)
        repository.saveKeepScreenOn(false)

        assertFalse(repository.keepScreenOn().first())
    }
}
