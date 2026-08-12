package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsVehicleClassTyreTemperaturePreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_lmu_windows_vehicle_class_tyre_temperature_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `車両クラス別タイヤ温度設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(highThresholdCelsiusByVehicleClass = mapOf("GTE" to 100)) }

            assertTrue(tempDir.resolve("lmu_windows_vehicle_class_tyre_temperature_preferences.pb").exists())
        }
}
