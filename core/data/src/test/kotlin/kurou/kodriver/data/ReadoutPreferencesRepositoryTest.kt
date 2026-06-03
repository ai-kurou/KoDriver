package kurou.kodriver.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutPreferencesRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @Test
    fun `readout_preferences_preferences_pbに書き込まれる`() = testScope.runTest {
        val repository = createReadoutPreferencesRepository(tempDir.absolutePath)
        repository.saveReadoutEnabledState("Le Mans Ultimate", "車両接近", true)

        assertTrue(tempDir.resolve("readout_preferences.preferences_pb").exists())
    }
}
