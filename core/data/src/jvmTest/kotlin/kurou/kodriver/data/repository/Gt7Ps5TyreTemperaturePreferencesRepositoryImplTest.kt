package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.Gt7Ps5TyreTemperaturePreferencesSerializer
import kurou.kodriver.domain.model.GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5TyreTemperaturePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_gt7_tyre_temperature_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = Gt7Ps5TyreTemperaturePreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("tyre_temperature.pb") },
        )
    private val repository = Gt7Ps5TyreTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は95度`() =
        testScope.runTest {
            assertEquals(
                GT7_PS5_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                repository.observeHighThresholdCelsius().first(),
            )
        }

    @Test
    fun `保存した高温閾値を取得できる`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(100)

            assertEquals(100, repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `高温閾値を上書き保存できる`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(100)
            repository.saveHighThresholdCelsius(105)

            assertEquals(105, repository.observeHighThresholdCelsius().first())
        }
}
