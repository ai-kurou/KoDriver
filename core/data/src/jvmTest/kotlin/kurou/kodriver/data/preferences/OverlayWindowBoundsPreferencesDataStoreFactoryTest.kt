package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class OverlayWindowBoundsPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_window_bounds_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `位置・サイズが正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createOverlayWindowBoundsPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(x = 10, y = 20) }

            assertTrue(tempDir.resolve("overlay_window_bounds_preferences.pb").exists())
        }
}
