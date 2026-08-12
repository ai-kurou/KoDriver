package kurou.kodriver.data.datasource

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class Gt7Ps5TyreTemperaturePreferencesDataStoreFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory("kodriver_gt7_tyre_temperature_preferences_factory_test")
            .toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `gt7_ps5_tyre_temperature_preferences設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createGt7Ps5TyreTemperaturePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(highThresholdCelsius = 100) }

            assertTrue(tempDir.resolve("gt7_ps5_tyre_temperature_preferences.pb").exists())
        }
}
