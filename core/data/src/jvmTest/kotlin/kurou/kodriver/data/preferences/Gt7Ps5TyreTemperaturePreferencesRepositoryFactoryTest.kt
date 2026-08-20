package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Gt7Ps5TyreTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_tyre_temperature_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は95度`() =
        runTest {
            val repository =
                createGt7Ps5TyreTemperaturePreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            assertEquals(
                GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                repository.observeHighThresholdCelsius().first(),
            )
        }

    @Test
    fun `保存した高温閾値を読み出せる`() =
        runTest {
            val repository =
                createGt7Ps5TyreTemperaturePreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveHighThresholdCelsius(Celsius(100))

            assertEquals(Celsius(100), repository.observeHighThresholdCelsius().first())
        }
}
