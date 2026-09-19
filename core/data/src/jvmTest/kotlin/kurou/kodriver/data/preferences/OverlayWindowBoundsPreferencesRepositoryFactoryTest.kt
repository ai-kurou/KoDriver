package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.OverlayWindowBounds
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class OverlayWindowBoundsPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_window_bounds_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は位置未設定の既定サイズ`() =
        runTest {
            val repository = createOverlayWindowBoundsPreferencesRepository(tempDir.absolutePath)

            assertEquals(OverlayWindowBounds(), repository.observeOverlayWindowBounds().first())
        }

    @Test
    fun `保存した値を読み出せる`() =
        runTest {
            val repository = createOverlayWindowBoundsPreferencesRepository(tempDir.absolutePath)
            val bounds = OverlayWindowBounds(x = 100, y = 200, width = 640, height = 200)
            repository.saveOverlayWindowBounds(bounds)

            assertEquals(bounds, repository.observeOverlayWindowBounds().first())
        }
}
