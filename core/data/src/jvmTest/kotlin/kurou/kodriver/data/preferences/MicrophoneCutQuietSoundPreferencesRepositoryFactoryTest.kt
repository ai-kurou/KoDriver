package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MicrophoneCutQuietSoundPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_microphone_cut_quiet_sound_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は既定の段階`() =
        runTest {
            val repository = createMicrophoneCutQuietSoundPreferencesRepository(tempDir.absolutePath)

            assertEquals(
                MICROPHONE_CUT_QUIET_SOUND_LEVEL_DEFAULT,
                repository.observeMicrophoneCutQuietSoundLevel().first(),
            )
        }

    @Test
    fun `保存した段階を読み出せる`() =
        runTest {
            val repository = createMicrophoneCutQuietSoundPreferencesRepository(tempDir.absolutePath)
            repository.saveMicrophoneCutQuietSoundLevel(9)

            assertEquals(9, repository.observeMicrophoneCutQuietSoundLevel().first())
        }

    @Test
    fun `コマンド受付中もカットする設定の既定値はオフ`() =
        runTest {
            val repository = createMicrophoneCutQuietSoundPreferencesRepository(tempDir.absolutePath)

            assertEquals(false, repository.observeMicrophoneCutQuietSoundInCommand().first())
        }

    @Test
    fun `保存したコマンド受付中もカットする設定を読み出せ、段階値は変わらない`() =
        runTest {
            val repository = createMicrophoneCutQuietSoundPreferencesRepository(tempDir.absolutePath)
            repository.saveMicrophoneCutQuietSoundLevel(9)
            repository.saveMicrophoneCutQuietSoundInCommand(true)

            assertEquals(true, repository.observeMicrophoneCutQuietSoundInCommand().first())
            assertEquals(9, repository.observeMicrophoneCutQuietSoundLevel().first())
        }
}
