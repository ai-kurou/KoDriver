package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsPitTimingPreferencesRepositoryImplTest {
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
    fun `保存したバーチャルエナジー予想残り周回数を取得できる`() =
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
    fun `enabledStates の初期値は空Map`() =
        runTest {
            assertEquals(emptyMap(), repository.observeEnabledStates().first())
        }

    @Test
    fun `saveEnabledState で保存した値を observeEnabledStates で取得できる`() =
        runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `saveEnabledState を複数回呼ぶと最後の値で上書きされる`() =
        runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, true)
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to false),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `異なるキーで保存した値がすべて保持される`() =
        runTest {
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy, true)
            repository.saveEnabledState(ReadoutItemKey.LmuWindows.PitTiming.TyreWear, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    ReadoutItemKey.LmuWindows.PitTiming.VirtualEnergy to true,
                    ReadoutItemKey.LmuWindows.PitTiming.TyreWear to false,
                ),
                repository.observeEnabledStates().first(),
            )
        }

    @Test
    fun `VirtualEnergyReadoutTextの既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT,
                repository.observeVirtualEnergyReadoutText().first(),
            )
            listOf("", " ", "残り{laps}周").forEach { text ->
                repository.saveVirtualEnergyReadoutText(text)
                assertEquals(text, repository.observeVirtualEnergyReadoutText().first())
            }
        }

    @Test
    fun `VirtualEnergyImminentReadoutTextの既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT,
                repository.observeVirtualEnergyImminentReadoutText().first(),
            )
            listOf("", " ", "残り{laps}周").forEach { text ->
                repository.saveVirtualEnergyImminentReadoutText(text)
                assertEquals(text, repository.observeVirtualEnergyImminentReadoutText().first())
            }
        }
}
