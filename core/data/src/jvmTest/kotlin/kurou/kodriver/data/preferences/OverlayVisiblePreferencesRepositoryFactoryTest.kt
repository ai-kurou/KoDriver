package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OverlayVisiblePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_overlay_visible_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はtrue`() =
        runTest {
            val repository = createOverlayVisiblePreferencesRepository(tempDir.absolutePath)

            assertTrue(repository.observeOverlayVisible().first())
        }

    @Test
    fun `保存した値を読み出せる`() =
        runTest {
            val repository = createOverlayVisiblePreferencesRepository(tempDir.absolutePath)
            repository.saveOverlayVisible(false)

            assertFalse(repository.observeOverlayVisible().first())
        }
}
