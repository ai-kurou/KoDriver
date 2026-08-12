package kurou.kodriver.data

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SimulatorPreferencesRepositoryFactoryTest {
    private val tempDir = Files.createTempDirectory("kodriver_simulator_repo_test").toFile()

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `simulator_preferences_pbに書き込まれる`() =
        runTest {
            val repository = createSimulatorPreferencesRepository(tempDir.absolutePath)
            repository.saveSelectedSimulator(Simulator.LmuWindows)

            assertEquals(Simulator.LmuWindows, repository.selectedSimulator().first())
        }
}
