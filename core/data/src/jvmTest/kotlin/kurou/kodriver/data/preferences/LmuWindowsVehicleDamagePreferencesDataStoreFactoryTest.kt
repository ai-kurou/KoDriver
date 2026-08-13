@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsVehicleDamagePreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_lmu_windows_vehicle_damage_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `lmu_windows_vehicle_damage_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsVehicleDamagePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(enabledStates = mapOf("overheat" to true)) }

            assertTrue(tempDir.resolve("lmu_windows_vehicle_damage_preferences.pb").exists())
        }
}
