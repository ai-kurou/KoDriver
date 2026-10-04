package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.repository.AceWindowsFlagReadoutTextPreferencesRepository
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AceWindowsFlagReadoutTextPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_ace_windows_flag_readout_text_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `AceWindowsFlagReadoutTextPreferencesRepositoryを返す`() {
        val repository = createAceWindowsFlagReadoutTextPreferencesRepository(tempDir.absolutePath)

        assertIs<AceWindowsFlagReadoutTextPreferencesRepository>(repository)
    }

    @Test
    fun `保存した文言を取得できる`() =
        runTest {
            val repository = createAceWindowsFlagReadoutTextPreferencesRepository(tempDir.absolutePath)

            repository.saveCheckeredFlagText("チェッカー、完走")

            assertEquals("チェッカー、完走", repository.observeCheckeredFlagText().first())
        }
}
