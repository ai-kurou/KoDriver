package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.VOICE_ID_UNSPECIFIED
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VoicePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_voice_preferences_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は未指定`() =
        runTest {
            val repository = createVoicePreferencesRepository(tempDir.absolutePath)

            assertEquals(VOICE_ID_UNSPECIFIED, repository.voiceId().first())
        }

    @Test
    fun `保存した音声IDを読み出せる`() =
        runTest {
            val repository = createVoicePreferencesRepository(tempDir.absolutePath)
            repository.saveVoiceId("Microsoft Haruka Desktop")

            assertEquals("Microsoft Haruka Desktop", repository.voiceId().first())
        }
}
