package kurou.kodriver.data.repository

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.datasource.ReadoutPreferencesSerializer
import kurou.kodriver.domain.model.ReadoutItemKey
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

        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)
        assertEquals(
            mapOf(ReadoutItemKey.VEHICLE_APPROACH to true),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )

        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, false)
        assertEquals(
            mapOf(ReadoutItemKey.VEHICLE_APPROACH to false),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
    }

    @Test
    fun `他シミュレータにデータがあっても未保存のシミュレータはemptyMapを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)

        assertTrue(repository.observeReadoutEnabledStates("rFactor 2").first().isEmpty())
    }

    @Test
    fun `未保存のシミュレータへの初回保存はemptyMapから開始され既存データを引き継がない`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)
        repository.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.FLAG, false)

        assertEquals(
            mapOf(ReadoutItemKey.FLAG to false),
            repository.observeReadoutEnabledStates("rFactor 2").first(),
        )
    }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.FLAG, false)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_DAMAGE, true)

        val states = repository.observeReadoutEnabledStates("lmu_windows").first()
        assertEquals(
            mapOf(
                ReadoutItemKey.VEHICLE_APPROACH to true,
                ReadoutItemKey.FLAG to false,
                ReadoutItemKey.VEHICLE_DAMAGE to true,
            ),
            states,
        )
    }

    @Test
    fun `シミュレーターごとに独立した状態を保存できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)
        repository.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.VEHICLE_APPROACH, false)

        assertEquals(
            mapOf(ReadoutItemKey.VEHICLE_APPROACH to true),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
        assertEquals(
            mapOf(ReadoutItemKey.VEHICLE_APPROACH to false),
            repository.observeReadoutEnabledStates("rFactor 2").first(),
        )
    }

    @Test
    fun `順序の初期値は空リスト・保存した順序を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())

        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
        )
        assertEquals(
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
            repository.observeReadoutOrder("lmu_windows").first(),
        )

        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE, ReadoutItemKey.VEHICLE_APPROACH),
        )
        assertEquals(
            listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE, ReadoutItemKey.VEHICLE_APPROACH),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `順序とenabledStatesは互いに独立して保存される`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_DAMAGE, false)
        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
        )

        assertEquals(
            mapOf(ReadoutItemKey.VEHICLE_APPROACH to true, ReadoutItemKey.VEHICLE_DAMAGE to false),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
        assertEquals(
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `他シミュレータに順序があっても未保存のシミュレータは空リストを返す`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VEHICLE_APPROACH))

        assertTrue(repository.observeReadoutOrder("rFactor 2").first().isEmpty())
    }

    @Test
    fun `未保存のシミュレータへの初回の順序保存はemptyListから開始され既存データを引き継がない`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG))
        repository.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.FLAG))

        assertEquals(listOf(ReadoutItemKey.FLAG), repository.observeReadoutOrder("rFactor 2").first())
    }

    @Test
    fun `シミュレーターごとに独立した順序を保存できる`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG))
        repository.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH))

        assertEquals(
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
        assertEquals(
            listOf(ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_APPROACH),
            repository.observeReadoutOrder("rFactor 2").first(),
        )
    }

    @Test
    fun `enabledState保存時に既存の順序が保持される`() = testScope.runTest {
        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
        )
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)

        assertEquals(
            listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG, ReadoutItemKey.VEHICLE_DAMAGE),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
        assertEquals(
            mapOf(ReadoutItemKey.VEHICLE_APPROACH to true),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
    }

    @Test
    fun `順序のみ保存済みのシミュレータはenabledStatesが空Mapを返す`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VEHICLE_APPROACH, ReadoutItemKey.FLAG))

        assertTrue(repository.observeReadoutEnabledStates("lmu_windows").first().isEmpty())
    }

    @Test
    fun `enabledStateのみ保存済みのシミュレータはitemOrderが空リストを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VEHICLE_APPROACH, true)

        assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())
    }
}
