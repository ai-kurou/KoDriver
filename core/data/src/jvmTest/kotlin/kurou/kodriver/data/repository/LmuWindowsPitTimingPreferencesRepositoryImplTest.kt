package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.LmuWindowsPitTimingPreferencesSerializer
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsPitTimingPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_lmu_windows_pit_timing_preferences_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val pitTimingDataStore =
        DataStoreFactory.create(
        serializer = LmuWindowsPitTimingPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("pit_timing.pb") },
    )
    private val repository = LmuWindowsPitTimingPreferencesRepositoryImpl(pitTimingDataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は両方とも3周`() =
        testScope.runTest {
        assertEquals(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT, repository.observeVirtualEnergyLaps().first())
        assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT, repository.observeTyreWearLaps().first())
    }

    @Test
    fun `保存したバーチャルエナジー予想残り周回数を取得できる`() =
        testScope.runTest {
        repository.saveVirtualEnergyLaps(1)

        assertEquals(1, repository.observeVirtualEnergyLaps().first())
    }

    @Test
    fun `保存したタイヤ摩耗予想残り周回数を取得できる`() =
        testScope.runTest {
        repository.saveTyreWearLaps(5)

        assertEquals(5, repository.observeTyreWearLaps().first())
    }
}
