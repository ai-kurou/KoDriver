package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5TyreTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_tyre_temperature_repository_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は95度`() =
        testScope.runTest {
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
        testScope.runTest {
            val repository =
                createGt7Ps5TyreTemperaturePreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveHighThresholdCelsius(100)

            assertEquals(100, repository.observeHighThresholdCelsius().first())
        }
}
