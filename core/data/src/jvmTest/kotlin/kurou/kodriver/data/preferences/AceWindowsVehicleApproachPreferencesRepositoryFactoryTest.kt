package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
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
                ACE_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT,
                repository.observeLongitudinalThresholdMeters().first(),
            )
            assertEquals(
                ACE_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT,
                repository.observeLateralThresholdMeters().first(),
            )
        }

    @Test
    fun `保存した閾値を読み出せる`() =
        runTest {
            val repository =
                createAceWindowsVehicleApproachPreferencesRepository(
                    directory = tempDir.absolutePath,
                )

            repository.saveLongitudinalThresholdMeters(7.0)
            repository.saveLateralThresholdMeters(6.0)

            assertEquals(7.0, repository.observeLongitudinalThresholdMeters().first())
            assertEquals(6.0, repository.observeLateralThresholdMeters().first())
        }
}
