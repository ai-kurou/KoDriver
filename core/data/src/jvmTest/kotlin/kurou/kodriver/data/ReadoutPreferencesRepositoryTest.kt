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
class ReadoutPreferencesRepositoryTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_repo_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `readout設定が正しいファイルに書き込まれる`() = testScope.runTest {
        val repository = createReadoutPreferencesRepository(tempDir.absolutePath)
        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)

        assertEquals(mapOf("vehicle_approach" to true), repository.observeReadoutEnabledStates("lmu").first())
    }
}
