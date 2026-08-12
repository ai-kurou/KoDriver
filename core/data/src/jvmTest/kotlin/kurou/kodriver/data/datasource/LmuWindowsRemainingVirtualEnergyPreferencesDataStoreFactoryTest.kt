package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsRemainingVirtualEnergyPreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_remaining_virtual_energy_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `バーチャルエナジー残量設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsRemainingVirtualEnergyPreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(thresholdPercentage = 50) }

            assertTrue(tempDir.resolve("lmu_windows_remaining_virtual_energy_preferences.pb").exists())
        }
}
