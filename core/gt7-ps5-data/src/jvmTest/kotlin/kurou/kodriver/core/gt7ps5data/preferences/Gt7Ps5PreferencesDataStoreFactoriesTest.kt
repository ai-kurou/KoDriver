package kurou.kodriver.core.gt7ps5data.preferences

import kotlinx.coroutines.test.runTest
import kurou.kodriver.core.gt7ps5data.datasource.createGt7Ps5UdpPortPreferencesDataStore
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class Gt7Ps5PreferencesDataStoreFactoriesTest {
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
            assertWritesTo("gt7_ps5_auto_my_best_lap_preferences.pb") { dir ->
                val dataStore = createGt7Ps5MyBestLapPreferencesDataStore(dir)
                dataStore.updateData { it.copy(voiceType = "casual") }
            }
            assertWritesTo("gt7_ps5_auto_remaining_fuel_laps_preferences.pb") { dir ->
                val dataStore = createGt7Ps5RemainingFuelLapsPreferencesDataStore(dir)
                dataStore.updateData { it.copy(remainingFuelLaps = 5) }
            }
            assertWritesTo("gt7_ps5_auto_tyre_temperature_preferences.pb") { dir ->
                val dataStore = createGt7Ps5TyreTemperaturePreferencesDataStore(dir)
                dataStore.updateData { it.copy(highThresholdCelsius = 120) }
            }
            assertWritesTo("gt7_udp_port_preferences.pb") { dir ->
                val dataStore = createGt7Ps5UdpPortPreferencesDataStore(dir)
                dataStore.updateData { it.copy(port = 33741) }
            }
        }
}
