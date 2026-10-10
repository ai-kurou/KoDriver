package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleClassBrakeWearPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_vehicle_class_brake_wear_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値はGTEのデフォルト閾値`() =
        runTest {
            val repository = createLmuWindowsVehicleClassBrakeWearPreferencesRepository(tempDir.absolutePath)

            assertEquals(
                lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(LmuWindowsVehicleClassData.Gte),
                repository.observeLowThresholdPercent().first()[LmuWindowsVehicleClassData.Gte],
            )
        }

    @Test
    fun `保存した値を読み出せる`() =
        runTest {
            val repository = createLmuWindowsVehicleClassBrakeWearPreferencesRepository(tempDir.absolutePath)

            repository.saveLowThresholdPercent(LmuWindowsVehicleClassData.Gte, 25)

            assertEquals(
                25,
                repository.observeLowThresholdPercent().first()[LmuWindowsVehicleClassData.Gte],
            )
        }
}
