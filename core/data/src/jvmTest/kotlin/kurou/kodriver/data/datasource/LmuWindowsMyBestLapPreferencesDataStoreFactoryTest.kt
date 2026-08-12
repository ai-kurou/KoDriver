package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsMyBestLapPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_my_best_lap_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `LMU自己ベストラップ設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsMyBestLapPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceType = "casual") }

            assertTrue(tempDir.resolve("lmu_windows_my_best_lap_preferences.pb").exists())
        }
}
