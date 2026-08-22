package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AceWindowsVehicleApproachPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_ace_vehicle_approach_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ace_windows_vehicle_approach_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createAceWindowsVehicleApproachPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(longitudinalThresholdMeters = 7.0) }

            assertTrue(tempDir.resolve("ace_windows_vehicle_approach_preferences.pb").exists())
        }
}
