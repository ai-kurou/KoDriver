package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsTyreTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_tyre_temperature_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は highThresholdCelsius が 95`() =
        runTest {
            val repository = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath)

            assertEquals(
                LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                repository.observeHighThresholdCelsius().first(),
            )
        }

    @Test
    fun `保存した highThresholdCelsius を読み出せる`() =
        runTest {
            val repository = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath)

            repository.saveHighThresholdCelsius(105)

            assertEquals(105, repository.observeHighThresholdCelsius().first())
        }
}
