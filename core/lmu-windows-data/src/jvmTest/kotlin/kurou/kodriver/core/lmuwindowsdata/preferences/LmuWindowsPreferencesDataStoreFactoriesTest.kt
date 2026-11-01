package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsPreferencesDataStoreFactoriesTest {
    private val tempDir = Files.createTempDirectory("kodriver_preferences_factories_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    private suspend fun assertWritesTo(
        fileName: String,
        write: suspend (directory: String) -> Unit,
    ) {
        val directory = tempDir.resolve(fileName).apply { mkdirs() }
        write(directory.absolutePath)
        assertTrue(File(directory, fileName).exists(), "$fileName が作成されていない")
    }

    @Test
    fun `各ファクトリの設定が期待するファイルに書き込まれる`() =
        runTest {
            assertWritesTo("lmu_windows_auto_flag_readout_text_preferences.pb") { dir ->
                val dataStore = createLmuWindowsFlagReadoutTextPreferencesDataStore(dir)
                dataStore.updateData { it.copy(sectorYellowFlagText = "イエロー、注意") }
            }
            assertWritesTo("lmu_windows_auto_my_best_lap_preferences.pb") { dir ->
                val dataStore = createLmuWindowsMyBestLapPreferencesDataStore(dir)
                dataStore.updateData { it.copy(lmuWindowsReadoutText = "更新{laptime}") }
            }
            assertWritesTo("lmu_windows_auto_pit_timing_preferences.pb") { dir ->
                val dataStore = createLmuWindowsPitTimingPreferencesDataStore(dir)
                dataStore.updateData { it.copy(virtualEnergyLaps = 5) }
            }
            assertWritesTo("lmu_windows_auto_remaining_fuel_preferences.pb") { dir ->
                val dataStore = createLmuWindowsRemainingFuelPreferencesDataStore(dir)
                dataStore.updateData { it.copy(thresholdPercentage = 50) }
            }
            assertWritesTo("lmu_windows_auto_remaining_virtual_energy_preferences.pb") { dir ->
                val dataStore = createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(dir)
                dataStore.updateData { it.copy(thresholdPercentage = 50) }
            }
            assertWritesTo("lmu_windows_auto_tyre_temperature_preferences.pb") { dir ->
                val dataStore = createLmuWindowsTyreTemperaturePreferencesDataStore(dir)
                dataStore.updateData { it.copy(overheatReadoutText = "タイヤ過熱注意") }
            }
            assertWritesTo("lmu_windows_auto_tyre_wear_preferences.pb") { dir ->
                val dataStore = createLmuWindowsTyreWearPreferencesDataStore(dir)
                dataStore.updateData { it.copy(thresholdPercentage = 50) }
            }
            assertWritesTo("lmu_windows_auto_vehicle_approach_preferences.pb") { dir ->
                val dataStore = createLmuWindowsVehicleApproachPreferencesDataStore(dir)
                dataStore.updateData { it.copy(skipFirstLap = false) }
            }
            assertWritesTo("lmu_windows_auto_vehicle_approach_thresholds_preferences.pb") { dir ->
                val dataStore = createLmuWindowsVehicleApproachThresholdsPreferencesDataStore(dir)
                dataStore.updateData { it.copy(longitudinalThresholdMeters = 20.0) }
            }
            assertWritesTo("lmu_windows_auto_vehicle_class_brake_temperature_preferences.pb") { dir ->
                val dataStore = createLmuWindowsVehicleClassBrakeTemperaturePreferencesDataStore(dir)
                dataStore.updateData { it.copy(highThresholdCelsiusByVehicleClass = mapOf("GTE" to 700)) }
            }
            assertWritesTo("lmu_windows_auto_vehicle_class_brake_wear_preferences.pb") { dir ->
                val dataStore = createLmuWindowsVehicleClassBrakeWearPreferencesDataStore(dir)
                dataStore.updateData { it.copy(lowThresholdPercentByVehicleClass = mapOf("GTE" to 35)) }
            }
            assertWritesTo("lmu_windows_auto_vehicle_class_tyre_temperature_preferences.pb") { dir ->
                val dataStore = createLmuWindowsVehicleClassTyreTemperaturePreferencesDataStore(dir)
                dataStore.updateData { it.copy(highThresholdCelsiusByVehicleClass = mapOf("GTE" to 100)) }
            }
            assertWritesTo("lmu_windows_auto_vehicle_damage_preferences.pb") { dir ->
                val dataStore = createLmuWindowsVehicleDamagePreferencesDataStore(dir)
                dataStore.updateData { it.copy(enabledStates = mapOf("overheat" to true)) }
            }
        }
}
