package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AceWindowsMyBestLapPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_my_best_lap_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ace_windows_my_best_lap_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createAceWindowsMyBestLapPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceType = "casual") }

            assertTrue(tempDir.resolve("ace_windows_my_best_lap_preferences.pb").exists())
        }
}
