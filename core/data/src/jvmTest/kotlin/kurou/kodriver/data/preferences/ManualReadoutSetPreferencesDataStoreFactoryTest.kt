package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualReadoutSetPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_manual_readout_set_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `manual_readout_set_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            Simulator.entries.forEach { simulator ->
                val dataStore = createManualReadoutSetPreferencesDataStore(tempDir.absolutePath, simulator)
                dataStore.updateData { it.copy(sets = listOf(ManualReadoutSetPreference("set-1"))) }

                assertTrue(tempDir.resolve("manual_readout_set_preferences_${simulator.id}.pb").exists())
            }
        }
}
