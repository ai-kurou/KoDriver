package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class VoicePreferencesDataStoreFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_voice_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `音声設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createVoicePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(voiceId = "ja-jp-x-jab-local") }

            assertTrue(tempDir.resolve("voice_preferences.pb").exists())
        }
}
