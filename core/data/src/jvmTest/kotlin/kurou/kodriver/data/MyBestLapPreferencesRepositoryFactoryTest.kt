package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.MyBestLapVoiceType
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MyBestLapPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_my_best_lap_preferences_repository_factory_test")
        .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は voiceType が FORMAL`() = testScope.runTest {
        val repository = createMyBestLapPreferencesRepository(tempDir.absolutePath)

        assertEquals(MyBestLapVoiceType.FORMAL, repository.observeVoiceType().first())
    }

    @Test
    fun `保存した voiceType を読み出せる`() = testScope.runTest {
        val repository = createMyBestLapPreferencesRepository(tempDir.absolutePath)

        repository.saveVoiceType(MyBestLapVoiceType.CASUAL)

        assertEquals(MyBestLapVoiceType.CASUAL, repository.observeVoiceType().first())
    }
}
