package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsMyBestLapPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_my_best_lap_preferences_repository_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は readoutText が 既定文言`() =
        runTest {
            val repository = createLmuWindowsMyBestLapPreferencesRepository(tempDir.absolutePath)

            assertEquals(LMU_WINDOWS_MY_BEST_LAP_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
        }

    @Test
    fun `保存した readoutText を読み出せる`() =
        runTest {
            val repository = createLmuWindowsMyBestLapPreferencesRepository(tempDir.absolutePath)

            repository.saveReadoutText("更新{laptime}")

            assertEquals("更新{laptime}", repository.observeReadoutText().first())
        }
}
