package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_TYRE_TEMPERATURE_DEFAULT_HIGH_THRESHOLD_CELSIUS
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsTyreTemperaturePreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory(
        "kodriver_lmu_windows_tyre_temperature_preferences_repository_factory_test",
    ).toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は highThresholdCelsius が 95`() = testScope.runTest {
        val repository = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath)

        assertEquals(
            LMU_WINDOWS_TYRE_TEMPERATURE_DEFAULT_HIGH_THRESHOLD_CELSIUS,
            repository.observeHighThresholdCelsius().first(),
        )
    }

    @Test
    fun `保存した highThresholdCelsius を読み出せる`() = testScope.runTest {
        val repository = createLmuWindowsTyreTemperaturePreferencesRepository(tempDir.absolutePath)

        repository.saveHighThresholdCelsius(105)

        assertEquals(105, repository.observeHighThresholdCelsius().first())
    }
}
