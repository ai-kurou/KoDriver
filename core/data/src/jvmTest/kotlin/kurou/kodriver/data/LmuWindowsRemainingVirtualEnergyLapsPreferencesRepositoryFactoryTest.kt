package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsRemainingVirtualEnergyLapsPreferencesRepositoryFactoryTest {

    private val tempDir = Files.createTempDirectory(
        "kodriver_lmu_windows_remaining_virtual_energy_laps_preferences_repository_factory_test",
    )
        .toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は3周`() = testScope.runTest {
        val repository = createLmuWindowsRemainingVirtualEnergyLapsPreferencesRepository(
            directory = tempDir.absolutePath,
        )

        assertEquals(3, repository.observeRemainingVirtualEnergyLaps().first())
    }

    @Test
    fun `保存したバーチャルエナジー残り周回数を読み出せる`() = testScope.runTest {
        val repository = createLmuWindowsRemainingVirtualEnergyLapsPreferencesRepository(
            directory = tempDir.absolutePath,
        )

        repository.saveRemainingVirtualEnergyLaps(5)

        assertEquals(5, repository.observeRemainingVirtualEnergyLaps().first())
    }
}
