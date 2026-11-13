package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SpeechRecognitionResumeDelayPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_speech_resume_delay_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は500ミリ秒`() =
        runTest {
            val repository = createSpeechRecognitionResumeDelayPreferencesRepository(tempDir.absolutePath)

            assertEquals(500, repository.resumeDelayMs().first())
        }

    @Test
    fun `保存した待ち時間を読み出せる`() =
        runTest {
            val repository = createSpeechRecognitionResumeDelayPreferencesRepository(tempDir.absolutePath)
            repository.saveResumeDelayMs(1_500)

            assertEquals(1_500, repository.resumeDelayMs().first())
        }
}
