package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class VoiceSpeedPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_voice_speed_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `読み上げ速度設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createVoiceSpeedPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceSpeed = 1.5f) }

            assertTrue(tempDir.resolve("voice_speed_preferences.pb").exists())
        }
}
