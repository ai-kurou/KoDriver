package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsRedFlagPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_red_flag_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `LMU赤旗設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsRedFlagPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceType = "red_flag") }

            assertTrue(tempDir.resolve("lmu_windows_red_flag_preferences.pb").exists())
        }
}
