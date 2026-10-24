package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ManualReadoutPlayButtonLayoutPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_manual_readout_play_button_layout_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `表示設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createManualReadoutPlayButtonLayoutPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(enabled = true) }

            assertTrue(tempDir.resolve("manual_readout_play_button_layout_preferences.pb").exists())
        }
}
