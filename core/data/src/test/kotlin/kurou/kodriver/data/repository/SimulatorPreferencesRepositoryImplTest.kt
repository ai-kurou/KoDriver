package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.SimulatorPreferencesSerializer
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SimulatorPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_simulator_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = SimulatorPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = SimulatorPreferencesRepositoryImpl(dataStore)

    @Test
    fun `初期値はnull・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertNull(repository.selectedSimulator().first())

        repository.saveSelectedSimulator("lmu")
        assertEquals("lmu", repository.selectedSimulator().first())

        repository.saveSelectedSimulator("rFactor 2")
        assertEquals("rFactor 2", repository.selectedSimulator().first())
    }
}
