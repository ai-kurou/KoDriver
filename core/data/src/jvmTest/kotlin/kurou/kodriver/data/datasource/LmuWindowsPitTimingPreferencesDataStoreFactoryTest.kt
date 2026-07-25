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
class LmuWindowsPitTimingPreferencesDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_pit_timing_preferences_factory_test")
        .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `lmu_windows_pit_timing_preferences設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createLmuWindowsPitTimingPreferencesDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(virtualEnergyLaps = 5) }

        assertTrue(tempDir.resolve("lmu_windows_pit_timing_preferences.pb").exists())
    }
}
