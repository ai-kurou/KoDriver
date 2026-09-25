package kurou.kodriver.data.preferences

import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertTrue

class LmuWindowsBrakeTemperaturePreferencesDataStoreFactoryTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_brake_temperature_preferences_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `ブレーキ温度設定が正しいファイルに書き込まれる`() =
        runTest {
            val dataStore = createLmuWindowsBrakeTemperaturePreferencesDataStore(tempDir.absolutePath)
            dataStore.updateData { it.copy(highThresholdCelsius = 600) }

            assertTrue(tempDir.resolve("lmu_windows_brake_temperature_preferences.pb").exists())
        }
}
