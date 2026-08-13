package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class Gt7Ps5MyBestLapPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_my_best_lap_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `gt7_ps5_my_best_lap_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createGt7Ps5MyBestLapPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceType = "casual") }

            assertTrue(tempDir.resolve("gt7_ps5_my_best_lap_preferences.pb").exists())
        }
}
