package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorPreferencesRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_simulator_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `simulator_preferences_preferences_pbに書き込まれる`() = testScope.runTest {
        val repository = createSimulatorPreferencesRepository(tempDir.absolutePath)
        repository.saveSelectedSimulator("Le Mans Ultimate")

        assertTrue(tempDir.resolve("simulator_preferences.preferences_pb").exists())
    }
}
