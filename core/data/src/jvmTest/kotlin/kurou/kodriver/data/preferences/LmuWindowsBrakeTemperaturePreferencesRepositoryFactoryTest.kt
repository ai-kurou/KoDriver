package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsBrakeTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_brake_temperature_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は highThresholdCelsius が 700`() =
        runTest {
            val repository = createLmuWindowsBrakeTemperaturePreferencesRepository(tempDir.absolutePath)

            assertEquals(700, repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `保存した highThresholdCelsius を読み出せる`() =
        runTest {
            val repository = createLmuWindowsBrakeTemperaturePreferencesRepository(tempDir.absolutePath)

            repository.saveHighThresholdCelsius(600)

            assertEquals(600, repository.observeHighThresholdCelsius().first())
        }
}
