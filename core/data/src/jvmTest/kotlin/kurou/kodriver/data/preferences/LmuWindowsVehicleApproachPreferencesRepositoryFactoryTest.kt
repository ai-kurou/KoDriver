package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LmuWindowsVehicleApproachPreferencesRepositoryFactoryTest {
    private val tempDir =
        Files
            .createTempDirectory(
                "kodriver_lmu_windows_vehicle_approach_preferences_repository_factory_test",
            ).toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は skipFirstLap が true`() =
        runTest {
            val repository = createLmuWindowsVehicleApproachPreferencesRepository(tempDir.absolutePath)

            assertEquals(true, repository.observeSkipFirstLap().first())
        }

    @Test
    fun `保存した skipFirstLap を読み出せる`() =
        runTest {
            val repository = createLmuWindowsVehicleApproachPreferencesRepository(tempDir.absolutePath)

            repository.saveSkipFirstLap(true)

            assertEquals(true, repository.observeSkipFirstLap().first())
        }
}
