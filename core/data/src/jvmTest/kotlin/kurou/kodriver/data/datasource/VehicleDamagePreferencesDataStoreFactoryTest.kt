@file:Suppress("FunctionNaming")

package kurou.kodriver.data.datasource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class VehicleDamagePreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_vehicle_damage_preferences_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `vehicle_damage_preferences設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createVehicleDamagePreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(enabledStates = mapOf("overheat" to true)) }

        assertTrue(tempDir.resolve("vehicle_damage_preferences.pb").exists())
    }
}
