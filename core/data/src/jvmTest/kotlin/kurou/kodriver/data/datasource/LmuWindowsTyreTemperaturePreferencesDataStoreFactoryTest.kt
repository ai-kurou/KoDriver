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
class LmuWindowsTyreTemperaturePreferencesDataStoreFactoryTest {

    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_tyre_temperature_preferences_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `タイヤ温度設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createLmuWindowsTyreTemperaturePreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(highThresholdCelsius = 100) }

        assertTrue(tempDir.resolve("lmu_windows_tyre_temperature_preferences.pb").exists())
    }
}
