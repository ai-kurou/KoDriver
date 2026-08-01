@file:Suppress("FunctionNaming")

package kurou.kodriver.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidSimulatorPreferencesRepositoryTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var tempFile: File
    private lateinit var repository: AndroidSimulatorPreferencesRepository

    @BeforeTest
    fun setUp() {
        tempFile = File.createTempFile("simulator_test", ".preferences_pb")
        val dataStore =
            PreferenceDataStoreFactory.create(
            scope = CoroutineScope(testDispatcher + SupervisorJob()),
            produceFile = { tempFile },
        )
        repository = AndroidSimulatorPreferencesRepository(dataStore)
    }

    @AfterTest
    fun tearDown() {
        tempFile.delete()
    }

    @Test
    fun `selectedSimulatorは初期状態でnullを返し保存後に選択したシミュレータを返す`() =
        runTest(testDispatcher) {
        assertNull(repository.selectedSimulator().first())

        repository.saveSelectedSimulator(Simulator.LmuWindows)

        assertEquals(Simulator.LmuWindows, repository.selectedSimulator().first())
    }
}
