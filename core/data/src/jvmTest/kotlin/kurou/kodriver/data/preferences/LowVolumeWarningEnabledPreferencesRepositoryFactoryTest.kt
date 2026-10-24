package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class LowVolumeWarningEnabledPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_low_volume_warning_enabled_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はtrue`() =
        runTest {
            val repository = createLowVolumeWarningEnabledPreferencesRepository(tempDir.absolutePath)

            assertTrue(repository.observeLowVolumeWarningEnabled().first())
        }

    @Test
    fun `保存した値を読み出せる`() =
        runTest {
            val repository = createLowVolumeWarningEnabledPreferencesRepository(tempDir.absolutePath)
            repository.saveLowVolumeWarningEnabled(false)

            assertFalse(repository.observeLowVolumeWarningEnabled().first())
        }
}
