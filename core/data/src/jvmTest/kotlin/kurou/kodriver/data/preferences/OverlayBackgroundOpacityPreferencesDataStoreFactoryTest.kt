package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OverlayBackgroundOpacityPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_background_opacity_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `背景透明度設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createOverlayBackgroundOpacityPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(opacity = 60) }

            assertTrue(tempDir.resolve("overlay_background_opacity_preferences.pb").exists())
        }
}
