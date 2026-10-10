package kurou.kodriver.data.preferences

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
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
            val (repository, readoutTextRepository) =
                createLmuWindowsVehicleApproachPreferencesRepository(
                    tempDir.absolutePath,
                )

            assertEquals(true, repository.observeSkipFirstLap().first())
            assertEquals("カーレフト", readoutTextRepository.observeStartLeftReadoutText().first())
        }

    @Test
    fun `共有するDataStoreで設定と文言を交互に保存しても値が保持される`() =
        runTest {
            val (repository, readoutTextRepository) =
                createLmuWindowsVehicleApproachPreferencesRepository(
                    tempDir.absolutePath,
                )

            readoutTextRepository.saveStartLeftReadoutText("左注意")
            repository.saveSkipFirstLap(false)
            repository.saveEnabledState(LmuWindowsReadoutItemKey.VehicleApproach.Sustained, false)
            readoutTextRepository.saveSustainedRightReadoutText("継続注意")

            assertEquals(false, repository.observeSkipFirstLap().first())
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Sustained to false),
                repository.observeEnabledStates().first(),
            )
            assertEquals("左注意", readoutTextRepository.observeStartLeftReadoutText().first())
            assertEquals("継続注意", readoutTextRepository.observeSustainedRightReadoutText().first())
        }
}
