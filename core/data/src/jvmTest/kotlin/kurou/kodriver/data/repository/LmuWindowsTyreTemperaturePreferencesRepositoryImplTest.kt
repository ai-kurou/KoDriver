package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.LmuWindowsTyreTemperaturePreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsTyreTemperaturePreferencesRepositoryImplTest {

    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_tyre_temperature_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = LmuWindowsTyreTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `highThresholdCelsius の初期値は 90`() = testScope.runTest {
        assertEquals(90, repository.observeHighThresholdCelsius().first())
    }

    @Test
    fun `saveHighThresholdCelsius で保存した値を observeHighThresholdCelsius で取得できる`() = testScope.runTest {
        repository.saveHighThresholdCelsius(110)
        assertEquals(110, repository.observeHighThresholdCelsius().first())
    }

    @Test
    fun `saveHighThresholdCelsius を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveHighThresholdCelsius(80)
        repository.saveHighThresholdCelsius(95)
        assertEquals(95, repository.observeHighThresholdCelsius().first())
    }
}
