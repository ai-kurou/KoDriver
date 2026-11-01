package kurou.kodriver.core.acewindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AceWindowsPreferencesDataStoreFactoriesTest {
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
            assertWritesTo("ace_windows_auto_flag_readout_text_preferences.pb") { dir ->
                val dataStore = createAceWindowsFlagReadoutTextPreferencesDataStore(dir)
                dataStore.updateData { it.copy(checkeredFlagText = "チェッカー、完走") }
            }
            assertWritesTo("ace_windows_auto_my_best_lap_preferences.pb") { dir ->
                val dataStore = createAceWindowsMyBestLapPreferencesDataStore(dir)
                dataStore.updateData { it.copy(voiceType = "casual") }
            }
            assertWritesTo("ace_windows_auto_remaining_fuel_laps_preferences.pb") { dir ->
                val dataStore = createAceWindowsRemainingFuelLapsPreferencesDataStore(dir)
                dataStore.updateData { it.copy(thresholdLaps = 5) }
            }
            assertWritesTo("ace_windows_auto_remaining_fuel_preferences.pb") { dir ->
                val dataStore = createAceWindowsRemainingFuelPreferencesDataStore(dir)
                dataStore.updateData { it.copy(thresholdPercentage = 50) }
            }
            assertWritesTo("ace_windows_auto_tyre_temperature_preferences.pb") { dir ->
                val dataStore = createAceWindowsTyreTemperaturePreferencesDataStore(dir)
                dataStore.updateData { it.copy(highThresholdCelsius = 100) }
            }
            assertWritesTo("ace_windows_auto_vehicle_approach_preferences.pb") { dir ->
                val dataStore = createAceWindowsVehicleApproachPreferencesDataStore(dir)
                dataStore.updateData { it.copy(thresholdMeters = 7.0) }
            }
        }
}
