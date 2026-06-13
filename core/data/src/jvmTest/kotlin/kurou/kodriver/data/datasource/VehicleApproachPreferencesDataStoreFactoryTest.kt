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
class VehicleApproachPreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_vehicle_approach_preferences_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `vehicle_approach_preferences設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createVehicleApproachPreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(skipFirstLap = false) }

        assertTrue(tempDir.resolve("vehicle_approach_preferences.pb").exists())
    }
}
