package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT
import kurou.kodriver.domain.model.Celsius
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsTyreTemperaturePreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_ace_tyre_temperature_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AceWindowsTyreTemperaturePreferencesSerializer,
            scope = testScope,
            produceFile = { tempDir.resolve("tyre_temperature.pb") },
        )
    private val repository = AceWindowsTyreTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は90度`() =
        testScope.runTest {
            assertEquals(
                ACE_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_DEFAULT,
                repository.observeHighThresholdCelsius().first(),
            )
        }

    @Test
    fun `保存した高温閾値を取得できる`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(Celsius(100))

            assertEquals(Celsius(100), repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `高温閾値を上書き保存できる`() =
        testScope.runTest {
            repository.saveHighThresholdCelsius(Celsius(100))
            repository.saveHighThresholdCelsius(Celsius(105))

            assertEquals(Celsius(105), repository.observeHighThresholdCelsius().first())
        }

    @Test
    fun `enabledStates の初期値は空Map`() =
        testScope.runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, true)
            repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, true)
            repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to true,
                    ReadoutItemKey.AceWindows.TyreTemperature.Root to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState後にsaveHighThresholdCelsiusを呼んでもenabledStatesは保持される`() =
        testScope.runTest {
            repository.saveEnabledState(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning, false)
            repository.saveHighThresholdCelsius(Celsius(100))

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.AceWindows.TyreTemperature.OverheatWarning to false),
                repository.observeEnabledStates().first(),
            )
        }
}
