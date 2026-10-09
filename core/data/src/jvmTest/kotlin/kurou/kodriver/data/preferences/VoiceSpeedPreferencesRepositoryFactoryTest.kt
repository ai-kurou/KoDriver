package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VoiceSpeedPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_voice_speed_preferences_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は1_0`() =
        runTest {
            val repository = createVoiceSpeedPreferencesRepository(tempDir.absolutePath)

            assertEquals(1.0f, repository.voiceSpeed().first())
        }

    @Test
    fun `保存した読み上げ速度を読み出せる・上書きで更新される`() =
        runTest {
            val repository = createVoiceSpeedPreferencesRepository(tempDir.absolutePath)
            repository.saveVoiceSpeed(1.25f)

            assertEquals(1.25f, repository.voiceSpeed().first())

            repository.saveVoiceSpeed(0.5f)
            assertEquals(0.5f, repository.voiceSpeed().first())
        }
}
