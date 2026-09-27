package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayTextSize
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayTextSizePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_overlay_text_size_preferences_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はMEDIUM`() =
        runTest {
            val repository = createOverlayTextSizePreferencesRepository(tempDir.absolutePath)

            assertEquals(OverlayTextSize.MEDIUM, repository.observeOverlayTextSize().first())
        }

    @Test
    fun `保存したオーバーレイ文字サイズを読み出せる`() =
        runTest {
            val repository = createOverlayTextSizePreferencesRepository(tempDir.absolutePath)

            repository.saveOverlayTextSize(OverlayTextSize.LARGE)

            assertEquals(OverlayTextSize.LARGE, repository.observeOverlayTextSize().first())
        }
}
