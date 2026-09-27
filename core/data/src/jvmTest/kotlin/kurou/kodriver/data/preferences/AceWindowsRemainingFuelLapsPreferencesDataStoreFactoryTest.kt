package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AceWindowsRemainingFuelLapsPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_ace_windows_remaining_fuel_laps_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ace_windows_remaining_fuel_laps_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createAceWindowsRemainingFuelLapsPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(thresholdLaps = 5) }

            assertTrue(tempDir.resolve("ace_windows_remaining_fuel_laps_preferences.pb").exists())
        }
}
