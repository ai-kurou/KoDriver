package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class SoundVolumePreferencesRepositoryFactoryTest {

    private val tempDir =
        Files.createTempDirectory("kodriver_sound_volume_preferences_repository_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は100`() = testScope.runTest {
        val repository = createSoundVolumePreferencesRepository(tempDir.absolutePath)

        assertEquals(100, repository.volume().first())
    }

    @Test
    fun `保存した音量を読み出せる`() = testScope.runTest {
        val repository = createSoundVolumePreferencesRepository(tempDir.absolutePath)
        repository.saveVolume(55)

        assertEquals(55, repository.volume().first())
    }
}
