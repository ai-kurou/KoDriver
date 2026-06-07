package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProximityThresholdsRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_proximity_thresholds_repo_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `proximity_thresholds_pb が指定ディレクトリに生成される`() = testScope.runTest {
        val repo = createProximityThresholdsRepository(tempDir.absolutePath)
        repo.saveLongitudinalThresholdMeters(15.0)

        assertTrue(tempDir.resolve("proximity_thresholds.pb").exists())
    }

    @Test
    fun `縦方向閾値を保存して読み出せる`() = testScope.runTest {
        val repo = createProximityThresholdsRepository(tempDir.absolutePath)

        repo.saveLongitudinalThresholdMeters(15.0)
        assertEquals(15.0, repo.observeLongitudinalThresholdMeters().first())
    }

    @Test
    fun `横方向閾値を保存して読み出せる`() = testScope.runTest {
        val repo = createProximityThresholdsRepository(tempDir.absolutePath)

        repo.saveLateralThresholdMeters(3.0)
        assertEquals(3.0, repo.observeLateralThresholdMeters().first())
    }
}
