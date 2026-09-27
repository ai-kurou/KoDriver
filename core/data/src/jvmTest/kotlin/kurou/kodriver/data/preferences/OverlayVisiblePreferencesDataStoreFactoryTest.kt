package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OverlayVisiblePreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_visible_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `表示設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createOverlayVisiblePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(visible = false) }

            assertTrue(tempDir.resolve("overlay_visible_preferences.pb").exists())
        }
}
