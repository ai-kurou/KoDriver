package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayBackgroundOpacityPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_overlay_background_opacity_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は50`() =
        runTest {
            val repository = createOverlayBackgroundOpacityPreferencesRepository(tempDir.absolutePath)

            assertEquals(50, repository.observeOverlayBackgroundOpacity().first())
        }

    @Test
    fun `保存した透明度を読み出せる`() =
        runTest {
            val repository = createOverlayBackgroundOpacityPreferencesRepository(tempDir.absolutePath)
            repository.saveOverlayBackgroundOpacity(75)

            assertEquals(75, repository.observeOverlayBackgroundOpacity().first())
        }
}
