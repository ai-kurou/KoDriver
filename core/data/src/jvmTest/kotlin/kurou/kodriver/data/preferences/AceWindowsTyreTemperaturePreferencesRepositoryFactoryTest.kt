package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsTyreTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_ace_tyre_temperature_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は90度`() =
        runTest {
            val repository =
                createAceWindowsTyreTemperaturePreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            assertEquals(
                ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                repository.observeHighThresholdCelsius().first(),
            )
        }

    @Test
    fun `保存した高温閾値を読み出せる`() =
        runTest {
            val repository =
                createAceWindowsTyreTemperaturePreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveHighThresholdCelsius(100)

            assertEquals(100, repository.observeHighThresholdCelsius().first())
        }
}
