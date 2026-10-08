package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_IMMINENT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_READOUT_TEXT_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsPitTimingReadoutTextPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_pit_timing_preferences_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val pitTimingDataStore =
        DataStoreFactory.create(
            serializer = LmuWindowsPitTimingPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("pit_timing.pb") },
        )
    private val repository = LmuWindowsPitTimingReadoutTextPreferencesRepositoryImpl(pitTimingDataStore)

    @AfterTest
    fun tearDown() {
        dataStoreScope.cancel()
        tempDir.deleteRecursively()
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

    @Test
    fun `TyreWearReadoutTextの既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_READOUT_TEXT_DEFAULT,
                repository.observeTyreWearReadoutText().first(),
            )
            listOf("", " ", "残り{laps}周").forEach { text ->
                repository.saveTyreWearReadoutText(text)
                assertEquals(text, repository.observeTyreWearReadoutText().first())
            }
        }

    @Test
    fun `TyreWearImminentReadoutTextの既定値と保存した空文字や文言を取得できる`() =
        runTest {
            assertEquals(
                LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_IMMINENT_READOUT_TEXT_DEFAULT,
                repository.observeTyreWearImminentReadoutText().first(),
            )
            listOf("", " ", "残り{laps}周").forEach { text ->
                repository.saveTyreWearImminentReadoutText(text)
                assertEquals(text, repository.observeTyreWearImminentReadoutText().first())
            }
        }
}
