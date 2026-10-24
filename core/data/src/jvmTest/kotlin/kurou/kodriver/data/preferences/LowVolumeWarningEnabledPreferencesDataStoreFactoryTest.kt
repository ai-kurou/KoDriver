package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LowVolumeWarningEnabledPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_low_volume_warning_enabled_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `警告設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLowVolumeWarningEnabledPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(enabled = false) }

            assertTrue(tempDir.resolve("low_volume_warning_enabled_preferences.pb").exists())
        }
}
