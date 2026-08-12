package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsVehicleApproachThresholdsPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_lmu_windows_vehicle_approach_thresholds_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `lmu_windows_vehicle_approach_thresholds_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(longitudinalThresholdMeters = 20.0) }

            assertTrue(tempDir.resolve("lmu_windows_vehicle_approach_thresholds_preferences.pb").exists())
        }
}
