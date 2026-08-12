package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsTyreWearPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_tyre_wear_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `タイヤ摩耗設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsTyreWearPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(thresholdPercentage = 30) }

            assertTrue(tempDir.resolve("lmu_windows_tyre_wear_preferences.pb").exists())
        }
}
