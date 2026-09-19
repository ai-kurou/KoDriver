package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayTextSizePreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_text_size_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `overlay_text_size_preferences_pbを作成してデフォルト値を読み込める`() =
        runTest {
            val dataStore = createOverlayTextSizePreferencesDataStore(tempDir.absolutePath)

            assertEquals("medium", dataStore.data.first().size)
        }
}
