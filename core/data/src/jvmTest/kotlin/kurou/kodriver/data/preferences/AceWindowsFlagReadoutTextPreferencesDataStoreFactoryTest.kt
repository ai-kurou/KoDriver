package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AceWindowsFlagReadoutTextPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_ace_windows_flag_readout_text_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ACEフラッグ読み上げ文言設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createAceWindowsFlagReadoutTextPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(checkeredFlagText = "チェッカー、完走") }

            assertTrue(tempDir.resolve("ace_windows_flag_readout_text_preferences.pb").exists())
        }
}
