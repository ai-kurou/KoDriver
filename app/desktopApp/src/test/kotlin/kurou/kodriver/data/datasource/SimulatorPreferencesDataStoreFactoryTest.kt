package kurou.kodriver.data.datasource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorPreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_simulator_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `シミュレータ設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createSimulatorPreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(selectedSimulator = "lmu") }

        assertTrue(tempDir.resolve("simulator_preferences.pb").exists())
    }
}
