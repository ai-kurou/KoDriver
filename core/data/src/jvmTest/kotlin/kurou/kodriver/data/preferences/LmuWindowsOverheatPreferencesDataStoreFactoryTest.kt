package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsOverheatPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_overheat_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `LMUオーバーヒート設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsOverheatPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceType = "standard") }

            assertTrue(tempDir.resolve("lmu_windows_overheat_preferences.pb").exists())
        }
}
