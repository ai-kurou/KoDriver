package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DebugStateCardOrderPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_debug_state_card_order_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `debug_state_card_order_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createDebugStateCardOrderPreferencesDataStore(tempDir.absolutePath, Simulator.LmuWindows)
            dataStore.updateData { it.copy(cardOrder = listOf("SESSION")) }

            val gt7Store = createDebugStateCardOrderPreferencesDataStore(tempDir.absolutePath, Simulator.Gt7Ps5)
            gt7Store.updateData { it.copy(cardOrder = listOf("CURRENT_LAP")) }
            assertTrue(tempDir.resolve("debug_state_card_order_preferences_gt7_ps5.pb").exists())
            assertTrue(tempDir.resolve("debug_state_card_order_preferences_lmu_windows.pb").exists())
        }
}
