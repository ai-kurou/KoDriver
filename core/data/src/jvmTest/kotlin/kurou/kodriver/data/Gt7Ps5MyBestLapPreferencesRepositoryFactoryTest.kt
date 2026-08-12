package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5MyBestLapPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_my_best_lap_preferences_repository_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は voiceType が FORMAL`() =
        runTest {
            val repository = createGt7Ps5MyBestLapPreferencesRepository(tempDir.absolutePath)

            assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
        }

    @Test
    fun `保存した voiceType を読み出せる`() =
        runTest {
            val repository = createGt7Ps5MyBestLapPreferencesRepository(tempDir.absolutePath)

            repository.saveVoiceType(MyBestLapVoiceType.CASUAL)

            assertEquals(MyBestLapVoiceType.CASUAL, repository.observeVoiceType().first())
        }
}
