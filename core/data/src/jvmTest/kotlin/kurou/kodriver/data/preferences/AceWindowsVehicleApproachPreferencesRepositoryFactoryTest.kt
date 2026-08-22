package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AceWindowsVehicleApproachPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_ace_vehicle_approach_repository_factory_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `デフォルト値は各Defaults定数と一致する`() =
        runTest {
            val repository =
                createAceWindowsVehicleApproachPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT,
                repository.observeThresholdMeters().first(),
            )
        }

    @Test
    fun `保存した閾値を読み出せる`() =
        runTest {
            val repository =
                createAceWindowsVehicleApproachPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveThresholdMeters(7.0)

            assertEquals(7.0, repository.observeThresholdMeters().first())
        }
}
