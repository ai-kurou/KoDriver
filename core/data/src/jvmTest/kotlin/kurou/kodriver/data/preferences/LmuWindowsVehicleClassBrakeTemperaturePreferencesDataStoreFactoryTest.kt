package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsVehicleClassBrakeTemperaturePreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_lmu_windows_vehicle_class_brake_temperature_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `車両クラス別ブレーキ温度設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsVehicleClassBrakeTemperaturePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(highThresholdCelsiusByVehicleClass = mapOf("GTE" to 700)) }

            assertTrue(tempDir.resolve("lmu_windows_vehicle_class_brake_temperature_preferences.pb").exists())
        }
}
