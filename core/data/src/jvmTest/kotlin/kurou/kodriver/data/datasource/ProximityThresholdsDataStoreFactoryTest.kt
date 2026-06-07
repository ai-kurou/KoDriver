package kurou.kodriver.data.datasource

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProximityThresholdsDataStoreFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_proximity_thresholds_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `proximity_thresholds設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val dataStore = createProximityThresholdsDataStore(tempDir.absolutePath)
        dataStore.updateData { it.copy(longitudinalThresholdMeters = 20.0) }

        assertTrue(tempDir.resolve("proximity_thresholds.pb").exists())
    }
}
