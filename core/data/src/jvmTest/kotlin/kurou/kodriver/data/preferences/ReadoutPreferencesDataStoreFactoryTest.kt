package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ReadoutPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_readout_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readout設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createReadoutPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { prefs ->
                prefs.copy(simulatorStates = prefs.simulatorStates + ("lmu_windows" to SimulatorReadoutState()))
            }

            assertTrue(tempDir.resolve("readout_preferences.pb").exists())
        }
}
