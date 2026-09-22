package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
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
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val thresholdLapsDataStore =
        DataStoreFactory.create(
            serializer = AceWindowsRemainingFuelLapsPreferencesSerializer,
            scope = testScope,
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
        testScope.runTest {
            assertEquals(ACE_WINDOWS_REMAINING_FUEL_LAPS_THRESHOLD_DEFAULT, repository.observeThresholdLaps().first())
        }

    @Test
    fun `保存した燃料残り周回数を取得できる`() =
        testScope.runTest {
            repository.saveThresholdLaps(1)

            assertEquals(1, repository.observeThresholdLaps().first())
        }

    @Test
    fun `燃料残り周回数を上書き保存できる`() =
        testScope.runTest {
            repository.saveThresholdLaps(1)
            repository.saveThresholdLaps(5)

            assertEquals(5, repository.observeThresholdLaps().first())
        }
}
