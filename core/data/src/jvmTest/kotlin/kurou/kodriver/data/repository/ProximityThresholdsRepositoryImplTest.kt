package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ProximityThresholdsSerializer
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProximityThresholdsRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_proximity_thresholds_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = ProximityThresholdsSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = ProximityThresholdsRepositoryImpl(dataStore)

    @Test
    fun `縦方向閾値の初期値はデフォルト値・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertEquals(10.0, repository.observeLongitudinalThresholdMeters().first())

        repository.saveLongitudinalThresholdMeters(50.0)
        assertEquals(50.0, repository.observeLongitudinalThresholdMeters().first())

        repository.saveLongitudinalThresholdMeters(30.0)
        assertEquals(30.0, repository.observeLongitudinalThresholdMeters().first())
    }

    @Test
    fun `横方向閾値の初期値はデフォルト値・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertEquals(2.0, repository.observeLateralThresholdMeters().first())

        repository.saveLateralThresholdMeters(3.5)
        assertEquals(3.5, repository.observeLateralThresholdMeters().first())

        repository.saveLateralThresholdMeters(1.0)
        assertEquals(1.0, repository.observeLateralThresholdMeters().first())
    }

    @Test
    fun `縦横の閾値は独立して保持される`() = testScope.runTest {
        repository.saveLongitudinalThresholdMeters(40.0)
        repository.saveLateralThresholdMeters(5.0)

        assertEquals(40.0, repository.observeLongitudinalThresholdMeters().first())
        assertEquals(5.0, repository.observeLateralThresholdMeters().first())
    }
}
