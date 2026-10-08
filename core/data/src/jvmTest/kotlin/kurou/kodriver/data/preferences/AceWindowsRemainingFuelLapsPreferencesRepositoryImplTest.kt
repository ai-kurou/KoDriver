package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsRemainingFuelLapsPreferencesRepositoryImplTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_ace_windows_remaining_fuel_laps_preferences_test",
            ).toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val thresholdLapsDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsRemainingFuelLapsPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("remaining_fuel_laps.pb") },
        )
    private val repository =
        AceWindowsRemainingFuelLapsPreferencesRepositoryImpl(
            thresholdLapsDataStore,
        )

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は3周`() =
        runTest {
            assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT, repository.observeThresholdLaps().first())
        }

    @Test
    fun `保存した燃料残り周回数を取得できる`() =
        runTest {
            repository.saveThresholdLaps(1)

            assertEquals(1, repository.observeThresholdLaps().first())
        }

    @Test
    fun `燃料残り周回数を上書き保存できる`() =
        runTest {
            repository.saveThresholdLaps(1)
            repository.saveThresholdLaps(5)

            assertEquals(5, repository.observeThresholdLaps().first())
        }

    @Test
    fun `文言の初期値はドメインの既定値`() =
        runTest {
            assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_READOUT_TEXT_DEFAULT, repository.observeReadoutText().first())
            assertEquals(
                ACE_WINDOWS_REMAINING_FUEL_LAPS_EMPTY_READOUT_TEXT_DEFAULT,
                repository.observeEmptyReadoutText().first(),
            )
        }

    @Test
    fun `文言を独立して上書き保存でき空欄も維持する`() =
        runTest {
            repository.saveThresholdLaps(5)
            repository.saveReadoutText("残り{laps}周")
            repository.saveEmptyReadoutText("燃料なし")
            assertEquals("残り{laps}周", repository.observeReadoutText().first())
            assertEquals("燃料なし", repository.observeEmptyReadoutText().first())
            repository.saveReadoutText("")
            assertEquals("", repository.observeReadoutText().first())
            assertEquals("燃料なし", repository.observeEmptyReadoutText().first())
            repository.saveEmptyReadoutText("")
            assertEquals("", repository.observeEmptyReadoutText().first())
            assertEquals(5, repository.observeThresholdLaps().first())
        }
}
