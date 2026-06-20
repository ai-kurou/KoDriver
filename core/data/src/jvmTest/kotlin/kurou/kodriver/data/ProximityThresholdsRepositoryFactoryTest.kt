package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProximityThresholdsPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_proximity_thresholds_repository_factory_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は縦方向5m・横方向5m`() = testScope.runTest {
        val repository = createProximityThresholdsPreferencesRepository(tempDir.absolutePath)

        assertEquals(5.0, repository.observeLongitudinalThresholdMeters().first())
        assertEquals(5.0, repository.observeLateralThresholdMeters().first())
    }

    @Test
    fun `保存した閾値を読み出せる`() = testScope.runTest {
        val repository = createProximityThresholdsPreferencesRepository(tempDir.absolutePath)

        repository.saveLongitudinalThresholdMeters(0.5)
        repository.saveLateralThresholdMeters(3.5)

        assertEquals(0.5, repository.observeLongitudinalThresholdMeters().first())
        assertEquals(3.5, repository.observeLateralThresholdMeters().first())
    }

    @Test
    fun `縦横の閾値は独立して保持される`() = testScope.runTest {
        val repository = createProximityThresholdsPreferencesRepository(tempDir.absolutePath)

        repository.saveLongitudinalThresholdMeters(0.3)
        repository.saveLateralThresholdMeters(4.0)

        assertEquals(0.3, repository.observeLongitudinalThresholdMeters().first())
        assertEquals(4.0, repository.observeLateralThresholdMeters().first())
    }
}
