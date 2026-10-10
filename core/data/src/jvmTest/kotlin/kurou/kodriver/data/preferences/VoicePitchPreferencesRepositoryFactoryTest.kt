package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class VoicePitchPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_voice_pitch_preferences_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は1_0`() =
        runTest {
            val repository = createVoicePitchPreferencesRepository(tempDir.absolutePath)

            assertEquals(1.0f, repository.voicePitch().first())
        }

    @Test
    fun `保存した声の高さを読み出せる・上書きで更新される`() =
        runTest {
            val repository = createVoicePitchPreferencesRepository(tempDir.absolutePath)
            repository.saveVoicePitch(1.25f)

            assertEquals(1.25f, repository.voicePitch().first())

            repository.saveVoicePitch(0.5f)
            assertEquals(0.5f, repository.voicePitch().first())
        }
}
