package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SimulatorPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_simulator_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `シミュレータ設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createSimulatorPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(selectedSimulator = "lmu_windows") }

            assertTrue(tempDir.resolve("simulator_preferences.pb").exists())
        }
}
