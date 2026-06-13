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
class SoundVolumeRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_sound_volume_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `sound_volume_preferences_pbに書き込まれる`() = testScope.runTest {
        val repository = createSoundVolumeRepository(tempDir.absolutePath)
        repository.saveVolume(55)

        assertEquals(55, repository.volume().first())
    }
}
