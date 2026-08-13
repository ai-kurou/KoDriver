package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class Gt7Ps5RemainingFuelLapsPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_gt7_remaining_fuel_laps_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `gt7_ps5_remaining_fuel_laps_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createGt7Ps5RemainingFuelLapsPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(remainingFuelLaps = 5) }

            assertTrue(tempDir.resolve("gt7_ps5_remaining_fuel_laps_preferences.pb").exists())
        }
}
