package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer
import kurou.kodriver.domain.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_LAPS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsRemainingVirtualEnergyLapsPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory(
        "kodriver_lmu_windows_remaining_virtual_energy_laps_preferences_test",
    ).toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val remainingVirtualEnergyLapsDataStore = DataStoreFactory.create(
        serializer = LmuWindowsRemainingVirtualEnergyLapsPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("remaining_virtual_energy_laps.pb") },
    )
    private val repository = LmuWindowsRemainingVirtualEnergyLapsPreferencesRepositoryImpl(
        remainingVirtualEnergyLapsDataStore,
    )

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は3周`() = testScope.runTest {
        assertEquals(
            LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_LAPS_DEFAULT,
            repository.observeRemainingVirtualEnergyLaps().first(),
        )
    }

    @Test
    fun `保存したバーチャルエナジー残り周回数を取得できる`() = testScope.runTest {
        repository.saveRemainingVirtualEnergyLaps(1)

        assertEquals(1, repository.observeRemainingVirtualEnergyLaps().first())
    }

    @Test
    fun `バーチャルエナジー残り周回数を上書き保存できる`() = testScope.runTest {
        repository.saveRemainingVirtualEnergyLaps(1)
        repository.saveRemainingVirtualEnergyLaps(5)

        assertEquals(5, repository.observeRemainingVirtualEnergyLaps().first())
    }
}
