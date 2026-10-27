package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.registerLmuWindowsReadoutItemKeys
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsTyreTemperaturePreferencesRepositoryImplTest {
    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val tempDir =
        Files.createTempDirectory("kodriver_lmu_windows_tyre_temperature_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsTyreTemperaturePreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = LmuWindowsTyreTemperaturePreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `enabledStates の初期値は空Map`() =
        runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `lowWarningPhases の初期値は空Map`() =
        runTest {
            assertEquals(emptyMap(), repository.observeLowWarningPhases().first())
        }

    @Test
    fun `saveLowWarningPhases で保存した値を observeLowWarningPhases で取得できる`() =
        runTest {
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
    fun `saveLowWarningPhases を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
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
    fun `saveLowWarningPhases で空集合を保存できる`() =
        runTest {
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
    fun `未知の設定キーとセッションを除外する`() =
        runTest {
            val key = LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning
            dataStore.updateData {
                it.copy(
                    enabledStates = mapOf(key.value to false, "future_temperature" to true),
                    lowWarningPhases = mapOf(SessionPhase.FORMATION.rawValue to true, -12345 to true),
                )
            }
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), repository.observeEnabledStates().first())
            assertEquals(mapOf(SessionPhase.FORMATION to true), repository.observeLowWarningPhases().first())
        }

    @Test
    fun `保存した設定をすべて既定値に戻せる`() =
        runTest {
            val readoutTextRepository = LmuWindowsTyreTemperatureReadoutTextPreferencesRepositoryImpl(dataStore)
            val defaultOverheatReadoutText = readoutTextRepository.observeOverheatReadoutText().first()
            val defaultColdReadoutText = readoutTextRepository.observeColdReadoutText().first()
            val defaultEnabledStates = repository.observeEnabledStates().first()
            val defaultLowWarningPhases = repository.observeLowWarningPhases().first()

            repository.saveEnabledState(LmuWindowsReadoutItemKey.TyreTemperature.OverheatWarning, false)
            repository.saveLowWarningPhases(emptySet())

            readoutTextRepository.saveOverheatReadoutText("変更した文言")
            readoutTextRepository.saveColdReadoutText("変更した文言")

            assertNotEquals(LmuWindowsTyreTemperaturePreferencesSerializer.defaultValue, dataStore.data.first())
            repository.resetToDefault()

            assertEquals(LmuWindowsTyreTemperaturePreferencesSerializer.defaultValue, dataStore.data.first())
            assertEquals(defaultEnabledStates, repository.observeEnabledStates().first())
            assertEquals(defaultLowWarningPhases, repository.observeLowWarningPhases().first())
            assertEquals(defaultOverheatReadoutText, readoutTextRepository.observeOverheatReadoutText().first())
            assertEquals(defaultColdReadoutText, readoutTextRepository.observeColdReadoutText().first())
        }
}
