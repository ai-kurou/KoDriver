package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SoundVolumePreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_sound_volume_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `音量設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createSoundVolumePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(volume = 60) }

            assertTrue(tempDir.resolve("sound_volume_preferences.pb").exists())
        }
}
