package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsRemainingFuelPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_ace_windows_remaining_fuel_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は thresholdPercentage が 30`() =
        runTest {
            val repository = createAceWindowsRemainingFuelPreferencesRepository(tempDir.absolutePath)

            assertEquals(30, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `保存した thresholdPercentage を読み出せる`() =
        runTest {
            val repository = createAceWindowsRemainingFuelPreferencesRepository(tempDir.absolutePath)

            repository.saveThresholdPercentage(50)

            assertEquals(50, repository.observeThresholdPercentage().first())
        }

    @Test
    fun `文言の初期値は既定値で保存と空欄の上書きは閾値を維持する`() =
        runTest {
            val repository = createAceWindowsRemainingFuelPreferencesRepository(tempDir.absolutePath)
            assertEquals(ACE_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
            repository.saveThresholdPercentage(45)
            repository.saveReadoutText("残り{percent}%")
            assertEquals("残り{percent}%", repository.observeReadoutText().first())
            repository.saveReadoutText("")
            assertEquals("", repository.observeReadoutText().first())
            assertEquals(45, repository.observeThresholdPercentage().first())
            repository.saveThresholdPercentage(60)
            assertEquals("", repository.observeReadoutText().first())
        }
}
