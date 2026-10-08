package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsMyBestLapPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_my_best_lap_preferences_repository_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は readoutText が既定文言`() =
        runTest {
            val repository = createAceWindowsMyBestLapPreferencesRepository(tempDir.absolutePath)

            assertEquals(ACE_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
        }

    @Test
    fun `保存した readoutText を読み出せる`() =
        runTest {
            val repository = createAceWindowsMyBestLapPreferencesRepository(tempDir.absolutePath)

            repository.saveReadoutText("更新{laptime}")

            assertEquals("更新{laptime}", repository.observeReadoutText().first())
        }
}
