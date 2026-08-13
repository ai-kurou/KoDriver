package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
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
            val dataStore = createDebugStateCardOrderPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(cardOrder = listOf("SESSION")) }

            assertTrue(tempDir.resolve("debug_state_card_order_preferences.pb").exists())
        }
}
