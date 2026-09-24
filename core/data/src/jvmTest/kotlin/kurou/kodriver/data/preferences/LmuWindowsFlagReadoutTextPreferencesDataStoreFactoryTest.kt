package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsFlagReadoutTextPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_flag_readout_text_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `LMUフラッグ読み上げ文言設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsFlagReadoutTextPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(sectorYellowFlagText = "イエロー、注意") }

            assertTrue(tempDir.resolve("lmu_windows_flag_readout_text_preferences.pb").exists())
        }
}
