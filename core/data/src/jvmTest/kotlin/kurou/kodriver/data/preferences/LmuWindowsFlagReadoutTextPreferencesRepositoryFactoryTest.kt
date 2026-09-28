package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.LmuWindowsFlagReadoutTextPreferencesRepository
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class LmuWindowsFlagReadoutTextPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_flag_readout_text_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `LmuWindowsFlagReadoutTextPreferencesRepositoryを返す`() {
        val repository = createLmuWindowsFlagReadoutTextPreferencesRepository(tempDir.absolutePath)

        assertIs<LmuWindowsFlagReadoutTextPreferencesRepository>(repository)
    }

    @Test
    fun `保存した文言を取得できる`() =
        runTest {
            val repository = createLmuWindowsFlagReadoutTextPreferencesRepository(tempDir.absolutePath)

            repository.saveSectorYellowFlagText("イエロー、注意")

            assertEquals("イエロー、注意", repository.observeSectorYellowFlagText().first())
        }
}
