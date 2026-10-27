package kurou.kodriver.core.lmuwindowsdata.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
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
class LmuWindowsPitTimingPreferencesRepositoryImplTest {
    @BeforeTest
    fun setUp() {
        registerLmuWindowsReadoutItemKeys()
    }

    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_pit_timing_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val pitTimingDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsPitTimingPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("pit_timing.pb") },
        )
    private val repository = LmuWindowsPitTimingPreferencesRepositoryImpl(pitTimingDataStore)

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は両方とも3周`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                repository.observeVirtualEnergyLaps().first(),
            )
            assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT, repository.observeTyreWearLaps().first())
        }

    @Test
    fun `燃料の初期値は両方とも3周`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                repository.observeVirtualEnergyLaps().first(),
            )
            assertEquals(LMU_WINDOWS_PIT_TIMING_FUEL_LAPS_DEFAULT, repository.observeFuelLaps().first())
        }

    @Test
    fun `保存したバーチャルエネルギー予想残り周回数を取得できる`() =
        runTest {
            repository.saveVirtualEnergyLaps(1)

            assertEquals(1, repository.observeVirtualEnergyLaps().first())
        }

    @Test
    fun `保存したタイヤ摩耗予想残り周回数を取得できる`() =
        runTest {
            repository.saveTyreWearLaps(5)

            assertEquals(5, repository.observeTyreWearLaps().first())
        }

    @Test
    fun `保存した燃料予想残り周回数を取得できる`() =
        runTest {
            repository.saveFuelLaps(5)

            assertEquals(5, repository.observeFuelLaps().first())
        }

    @Test
    fun `enabledStates の初期値は空Map`() =
        runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.TyreWear, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to true,
                    LmuWindowsReadoutItemKey.PitTiming.TyreWear to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `燃料の異なるキーで保存した値がすべて保持される`() =
        runTest {
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, true)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.Fuel, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy to true,
                    LmuWindowsReadoutItemKey.PitTiming.Fuel to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `有効状態の未知のキーを除外して既知のキーを取得する`() =
        runTest {
            val key = LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy
            pitTimingDataStore.updateData {
                it.copy(enabledStates = mapOf(key.value to false, "future_pit_timing" to true))
            }
            assertEquals(mapOf<ReadoutItemKey, Boolean>(key to false), repository.observeEnabledStates().first())
        }

    @Test
    fun `保存した設定をすべて既定値に戻せる`() =
        runTest {
            val readoutTextRepository = LmuWindowsPitTimingReadoutTextPreferencesRepositoryImpl(pitTimingDataStore)
            val defaultVirtualEnergyReadoutText = readoutTextRepository.observeVirtualEnergyReadoutText().first()
            val defaultVirtualEnergyImminentReadoutText =
                readoutTextRepository
                    .observeVirtualEnergyImminentReadoutText()
                    .first()
            val defaultTyreWearReadoutText = readoutTextRepository.observeTyreWearReadoutText().first()
            val defaultFuelReadoutText = readoutTextRepository.observeFuelReadoutText().first()
            val defaultTyreWearImminentReadoutText = readoutTextRepository.observeTyreWearImminentReadoutText().first()
            val defaultFuelImminentReadoutText = readoutTextRepository.observeFuelImminentReadoutText().first()
            val defaultVirtualEnergyLaps = repository.observeVirtualEnergyLaps().first()
            val defaultTyreWearLaps = repository.observeTyreWearLaps().first()
            val defaultFuelLaps = repository.observeFuelLaps().first()
            val defaultEnabledStates = repository.observeEnabledStates().first()

            repository.saveVirtualEnergyLaps(7)
            repository.saveTyreWearLaps(7)
            repository.saveFuelLaps(7)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy, false)

            readoutTextRepository.saveVirtualEnergyReadoutText("変更した文言")
            readoutTextRepository.saveVirtualEnergyImminentReadoutText("変更した文言")
            readoutTextRepository.saveTyreWearReadoutText("変更した文言")
            readoutTextRepository.saveFuelReadoutText("変更した文言")
            readoutTextRepository.saveTyreWearImminentReadoutText("変更した文言")
            readoutTextRepository.saveFuelImminentReadoutText("変更した文言")

            assertNotEquals(LmuWindowsPitTimingPreferencesSerializer.defaultValue, pitTimingDataStore.data.first())
            repository.resetToDefault()

            assertEquals(LmuWindowsPitTimingPreferencesSerializer.defaultValue, pitTimingDataStore.data.first())
            assertEquals(defaultVirtualEnergyLaps, repository.observeVirtualEnergyLaps().first())
            assertEquals(defaultTyreWearLaps, repository.observeTyreWearLaps().first())
            assertEquals(defaultFuelLaps, repository.observeFuelLaps().first())
            assertEquals(defaultEnabledStates, repository.observeEnabledStates().first())
            assertEquals(
                defaultVirtualEnergyReadoutText,
                readoutTextRepository.observeVirtualEnergyReadoutText().first(),
            )
            assertEquals(
                defaultVirtualEnergyImminentReadoutText,
                readoutTextRepository.observeVirtualEnergyImminentReadoutText().first(),
            )
            assertEquals(defaultTyreWearReadoutText, readoutTextRepository.observeTyreWearReadoutText().first())
            assertEquals(defaultFuelReadoutText, readoutTextRepository.observeFuelReadoutText().first())
            assertEquals(
                defaultTyreWearImminentReadoutText,
                readoutTextRepository.observeTyreWearImminentReadoutText().first(),
            )
            assertEquals(defaultFuelImminentReadoutText, readoutTextRepository.observeFuelImminentReadoutText().first())
        }
}
