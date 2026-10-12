package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayTextColorPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_overlay_text_color_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は既定の文字色`() =
        runTest {
            val repository = createOverlayTextColorPreferencesRepository(tempDir.absolutePath)

            assertEquals(0xFFFFC107L, repository.observeOverlayTextColor().first())
        }

    @Test
    fun `保存した文字色を読み出せる`() =
        runTest {
            val repository = createOverlayTextColorPreferencesRepository(tempDir.absolutePath)
            repository.saveOverlayTextColor(0xFF123456L)

            assertEquals(0xFF123456L, repository.observeOverlayTextColor().first())
        }
}
