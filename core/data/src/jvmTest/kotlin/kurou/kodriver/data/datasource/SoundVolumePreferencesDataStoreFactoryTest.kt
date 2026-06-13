package kurou.kodriver.data.datasource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SoundVolumePreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_sound_volume_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `音量設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createSoundVolumePreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(volume = 60) }

        assertTrue(tempDir.resolve("sound_volume_preferences.pb").exists())
    }
}
