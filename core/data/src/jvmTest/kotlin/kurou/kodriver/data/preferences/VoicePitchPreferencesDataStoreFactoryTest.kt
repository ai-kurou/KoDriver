package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class VoicePitchPreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_voice_pitch_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `声の高さ設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createVoicePitchPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voicePitch = 1.5f) }

            assertTrue(tempDir.resolve("voice_pitch_preferences.pb").exists())
        }
}
