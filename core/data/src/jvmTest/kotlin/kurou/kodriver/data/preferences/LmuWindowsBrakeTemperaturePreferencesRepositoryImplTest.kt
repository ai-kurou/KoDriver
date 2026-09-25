@file:Suppress("FunctionNaming")

package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsBrakeTemperaturePreferencesRepositoryImplTest {
    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_brake_temperature_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsBrakeTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsBrakeTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `highThresholdCelsius の初期値は 700`() =
        runTest {
            assertEquals(700, repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `saveHighThresholdCelsius で保存した値を observeHighThresholdCelsius で取得できる`() =
        runTest {
            repository.saveHighThresholdCelsius(600)
            assertEquals(600, repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `saveHighThresholdCelsius を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveHighThresholdCelsius(900)
            repository.saveHighThresholdCelsius(700)
            assertEquals(700, repository.observeHighThresholdCelsius().first())
        }
}
