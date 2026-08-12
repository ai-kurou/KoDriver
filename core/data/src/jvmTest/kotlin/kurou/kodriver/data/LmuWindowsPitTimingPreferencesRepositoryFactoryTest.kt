package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsPitTimingPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_pit_timing_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は両方とも3周`() =
        runTest {
            val repository = createLmuWindowsPitTimingPreferencesRepository(directory = tempDir.absolutePath)

            assertEquals(
                LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT,
                repository.observeVirtualEnergyLaps().first(),
            )
            assertEquals(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT, repository.observeTyreWearLaps().first())
        }

    @Test
    fun `保存した予想残り周回数を読み出せる`() =
        runTest {
            val repository = createLmuWindowsPitTimingPreferencesRepository(directory = tempDir.absolutePath)

            repository.saveVirtualEnergyLaps(5)
            repository.saveTyreWearLaps(1)

            assertEquals(5, repository.observeVirtualEnergyLaps().first())
            assertEquals(1, repository.observeTyreWearLaps().first())
        }
}
