package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SoundVolumePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_sound_volume_preferences_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は100`() =
        runTest {
            val repository = createSoundVolumePreferencesRepository(tempDir.absolutePath)

            assertEquals(100, repository.volume().first())
        }

    @Test
    fun `保存した音量を読み出せる`() =
        runTest {
            val repository = createSoundVolumePreferencesRepository(tempDir.absolutePath)
            repository.saveVolume(55)

            assertEquals(55, repository.volume().first())
        }
}
