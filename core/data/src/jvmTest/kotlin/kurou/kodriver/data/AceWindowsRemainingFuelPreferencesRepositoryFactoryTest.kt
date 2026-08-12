package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
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
}
