package kurou.kodriver.core.lmuwindowsdata.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsRemainingFuelPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_remaining_fuel_preferences_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `燃料残量設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsRemainingFuelPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(thresholdPercentage = 50) }

            assertTrue(tempDir.resolve("lmu_windows_auto_remaining_fuel_preferences.pb").exists())
        }
}
