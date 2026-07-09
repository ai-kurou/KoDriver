@file:Suppress("FunctionNaming")

package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.LmuWindowsTyreTemperaturePreferencesSerializer
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.SessionPhase
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

    @Test
    fun `enabledStates の初期値は空Map`() = testScope.runTest {
        assertEquals(emptyMap(), repository.observeEnabledStates().first())
    }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() = testScope.runTest {
        repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false),
            repository.observeEnabledStates().first(),
        )
    }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, true)
        repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to false),
            repository.observeEnabledStates().first(),
        )
    }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() = testScope.runTest {
        repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning, true)
        repository.saveEnabledState(ReadoutItemKey.LmuWindows.TyreTemperature.Root, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning to true,
                ReadoutItemKey.LmuWindows.TyreTemperature.Root to false,
            ),
            repository.observeEnabledStates().first(),
        )
    }

    @Test
    fun `lowWarningPhases の初期値は空Map`() = testScope.runTest {
        assertEquals(emptyMap(), repository.observeLowWarningPhases().first())
    }

    @Test
    fun `saveLowWarningPhases で保存した値を observeLowWarningPhases で取得できる`() = testScope.runTest {
        repository.saveLowWarningPhases(setOf(SessionPhase.FORMATION))
        assertEquals(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to false,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to true,
            ),
            repository.observeLowWarningPhases().first(),
        )
    }

    @Test
    fun `saveLowWarningPhases を複数回呼ぶと最後の値で上書きされる`() = testScope.runTest {
        repository.saveLowWarningPhases(setOf(SessionPhase.GARAGE))
        repository.saveLowWarningPhases(setOf(SessionPhase.WARM_UP, SessionPhase.GRID_WALK))
        assertEquals(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to true,
                SessionPhase.GRID_WALK to true,
                SessionPhase.FORMATION to false,
            ),
            repository.observeLowWarningPhases().first(),
        )
    }

    @Test
    fun `saveLowWarningPhases で空集合を保存できる`() = testScope.runTest {
        repository.saveLowWarningPhases(emptySet())
        assertEquals(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to false,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to false,
            ),
            repository.observeLowWarningPhases().first(),
        )
    }

    @Test
    fun `saveLowWarningPhases後にsaveHighThresholdCelsiusを呼んでもphasesは保持される`() = testScope.runTest {
        repository.saveLowWarningPhases(setOf(SessionPhase.FORMATION))
        repository.saveHighThresholdCelsius(100)
        assertEquals(
            mapOf(
                SessionPhase.GARAGE to false,
                SessionPhase.WARM_UP to false,
                SessionPhase.GRID_WALK to false,
                SessionPhase.FORMATION to true,
            ),
            repository.observeLowWarningPhases().first(),
        )
    }
}
