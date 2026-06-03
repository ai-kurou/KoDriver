package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ReadoutPreferencesSerializer
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutPreferencesRepositoryImplTest {

    private val tempDir = Files.createTempDirectory("kodriver_readout_prefs_test").toFile()
    private val testScope = TestScope(UnconfinedTestDispatcher())
    private val dataStore = DataStoreFactory.create(
        serializer = ReadoutPreferencesSerializer,
        scope = testScope,
        produceFile = { tempDir.resolve("test.pb") },
    )
    private val repository = ReadoutPreferencesRepositoryImpl(dataStore)

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeReadoutEnabledStates("lmu").first().isEmpty())

        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)
        assertEquals(mapOf("vehicle_approach" to true), repository.observeReadoutEnabledStates("lmu").first())

        repository.saveReadoutEnabledState("lmu", "vehicle_approach", false)
        assertEquals(mapOf("vehicle_approach" to false), repository.observeReadoutEnabledStates("lmu").first())
    }

    @Test
    fun `他シミュレータにデータがあっても未保存のシミュレータはemptyMapを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)

        assertTrue(repository.observeReadoutEnabledStates("rFactor 2").first().isEmpty())
    }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)
        repository.saveReadoutEnabledState("lmu", "laps_remaining", false)

        val states = repository.observeReadoutEnabledStates("lmu").first()
        assertEquals(mapOf("vehicle_approach" to true, "laps_remaining" to false), states)
    }

    @Test
    fun `シミュレーターごとに独立した状態を保存できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu", "vehicle_approach", true)
        repository.saveReadoutEnabledState("rFactor 2", "vehicle_approach", false)

        assertEquals(mapOf("vehicle_approach" to true), repository.observeReadoutEnabledStates("lmu").first())
        assertEquals(mapOf("vehicle_approach" to false), repository.observeReadoutEnabledStates("rFactor 2").first())
    }
}
