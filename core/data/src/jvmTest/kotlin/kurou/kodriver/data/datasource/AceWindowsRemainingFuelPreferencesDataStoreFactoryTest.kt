package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class AceWindowsRemainingFuelPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_ace_windows_remaining_fuel_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `残り燃料設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createAceWindowsRemainingFuelPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(thresholdPercentage = 50) }

            assertTrue(tempDir.resolve("ace_windows_remaining_fuel_preferences.pb").exists())
        }
}
