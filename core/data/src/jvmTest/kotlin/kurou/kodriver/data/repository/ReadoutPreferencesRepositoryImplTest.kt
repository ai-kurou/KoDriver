package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ReadoutPreferencesSerializer
import java.nio.file.Files
import kotlin.test.AfterTest
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

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeReadoutEnabledStates("lmu_windows").first().isEmpty())

        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)
        assertEquals(mapOf("vehicle_approach" to true), repository.observeReadoutEnabledStates("lmu_windows").first())

        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", false)
        assertEquals(mapOf("vehicle_approach" to false), repository.observeReadoutEnabledStates("lmu_windows").first())
    }

    @Test
    fun `他シミュレータにデータがあっても未保存のシミュレータはemptyMapを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)

        assertTrue(repository.observeReadoutEnabledStates("rFactor 2").first().isEmpty())
    }

    @Test
    fun `未保存のシミュレータへの初回保存はemptyMapから開始され既存データを引き継がない`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)
        repository.saveReadoutEnabledState("rFactor 2", "flag", false)

        assertEquals(mapOf("flag" to false), repository.observeReadoutEnabledStates("rFactor 2").first())
    }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)
        repository.saveReadoutEnabledState("lmu_windows", "flag", false)
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_damage", true)

        val states = repository.observeReadoutEnabledStates("lmu_windows").first()
        assertEquals(mapOf("vehicle_approach" to true, "flag" to false, "vehicle_damage" to true), states)
    }

    @Test
    fun `シミュレーターごとに独立した状態を保存できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)
        repository.saveReadoutEnabledState("rFactor 2", "vehicle_approach", false)

        assertEquals(mapOf("vehicle_approach" to true), repository.observeReadoutEnabledStates("lmu_windows").first())
        assertEquals(mapOf("vehicle_approach" to false), repository.observeReadoutEnabledStates("rFactor 2").first())
    }

    @Test
    fun `順序の初期値は空リスト・保存した順序を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())

        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag", "vehicle_damage"))
        assertEquals(
            listOf("vehicle_approach", "flag", "vehicle_damage"),
            repository.observeReadoutOrder("lmu_windows").first(),
        )

        repository.saveReadoutOrder("lmu_windows", listOf("flag", "vehicle_damage", "vehicle_approach"))
        assertEquals(
            listOf("flag", "vehicle_damage", "vehicle_approach"),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `順序とenabledStatesは互いに独立して保存される`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_damage", false)
        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag", "vehicle_damage"))

        assertEquals(
            mapOf("vehicle_approach" to true, "vehicle_damage" to false),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
        assertEquals(
            listOf("vehicle_approach", "flag", "vehicle_damage"),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `他シミュレータに順序があっても未保存のシミュレータは空リストを返す`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach"))

        assertTrue(repository.observeReadoutOrder("rFactor 2").first().isEmpty())
    }

    @Test
    fun `未保存のシミュレータへの初回の順序保存はemptyListから開始され既存データを引き継がない`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag"))
        repository.saveReadoutOrder("rFactor 2", listOf("flag"))

        assertEquals(listOf("flag"), repository.observeReadoutOrder("rFactor 2").first())
    }

    @Test
    fun `シミュレーターごとに独立した順序を保存できる`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag"))
        repository.saveReadoutOrder("rFactor 2", listOf("flag", "vehicle_approach"))

        assertEquals(listOf("vehicle_approach", "flag"), repository.observeReadoutOrder("lmu_windows").first())
        assertEquals(listOf("flag", "vehicle_approach"), repository.observeReadoutOrder("rFactor 2").first())
    }

    @Test
    fun `enabledState保存時に既存の順序が保持される`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag", "vehicle_damage"))
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)

        assertEquals(
            listOf("vehicle_approach", "flag", "vehicle_damage"),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
        assertEquals(mapOf("vehicle_approach" to true), repository.observeReadoutEnabledStates("lmu_windows").first())
    }

    @Test
    fun `順序のみ保存済みのシミュレータはenabledStatesが空Mapを返す`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf("vehicle_approach", "flag"))

        assertTrue(repository.observeReadoutEnabledStates("lmu_windows").first().isEmpty())
    }

    @Test
    fun `enabledStateのみ保存済みのシミュレータはitemOrderが空リストを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", "vehicle_approach", true)

        assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())
    }
}
