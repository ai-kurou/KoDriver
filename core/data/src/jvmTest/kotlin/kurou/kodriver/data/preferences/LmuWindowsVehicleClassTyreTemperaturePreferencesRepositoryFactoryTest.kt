package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassTyreTemperaturePreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_vehicle_class_tyre_temperature_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はGTEのデフォルトしきい値`() =
        runTest {
            val repository = createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(tempDir.absolutePath)

            assertEquals(
                lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(LmuWindowsVehicleClassData.Gte),
                repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `保存した値を読み出せる`() =
        runTest {
            val repository = createLmuWindowsVehicleClassTyreTemperaturePreferencesRepository(tempDir.absolutePath)

            repository.saveHighThresholdCelsius(LmuWindowsVehicleClassData.Gte, 105)

            assertEquals(105, repository.observeHighThresholdCelsius().first()[LmuWindowsVehicleClassData.Gte])
        }
}
