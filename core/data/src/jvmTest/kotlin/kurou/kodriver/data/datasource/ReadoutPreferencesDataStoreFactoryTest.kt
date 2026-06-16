package kurou.kodriver.data.datasource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.model.SimulatorReadoutState
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutPreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readout設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createReadoutPreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { prefs ->
            prefs.copy(simulatorStates = prefs.simulatorStates + ("lmu_windows" to SimulatorReadoutState()))
        }

        assertTrue(tempDir.resolve("readout_preferences.pb").exists())
    }
}
