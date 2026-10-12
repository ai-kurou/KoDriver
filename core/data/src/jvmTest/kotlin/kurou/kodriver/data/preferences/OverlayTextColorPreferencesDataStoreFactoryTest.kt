package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OverlayTextColorPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_text_color_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `文字色設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createOverlayTextColorPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(argb = 0xFF123456L) }

            assertTrue(tempDir.resolve("overlay_text_color_preferences.pb").exists())
        }
}
