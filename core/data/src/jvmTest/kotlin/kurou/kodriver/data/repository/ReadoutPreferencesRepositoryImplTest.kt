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

        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to true),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )

        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, false)
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to false),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
    }

    @Test
    fun `他シミュレータにデータがあっても未保存のシミュレータはemptyMapを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)

        assertTrue(repository.observeReadoutEnabledStates("rFactor 2").first().isEmpty())
    }

    @Test
    fun `未保存のシミュレータへの初回保存はemptyMapから開始され既存データを引き継がない`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repository.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.Flag, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.Flag to false),
            repository.observeReadoutEnabledStates("rFactor 2").first(),
        )
    }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.Flag, false)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleDamage, true)

        val states = repository.observeReadoutEnabledStates("lmu_windows").first()
        assertEquals(
            mapOf(
                ReadoutItemKey.VehicleApproach to true,
                ReadoutItemKey.Flag to false,
                ReadoutItemKey.VehicleDamage to true,
            ),
            states,
        )
    }

    @Test
    fun `シミュレーターごとに独立した状態を保存できる`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repository.saveReadoutEnabledState("rFactor 2", ReadoutItemKey.VehicleApproach, false)

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to true),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to false),
            repository.observeReadoutEnabledStates("rFactor 2").first(),
        )
    }

    @Test
    fun `順序の初期値は空リスト・保存した順序を返す・上書きで更新される`() = testScope.runTest {
        assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())

        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
        )
        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
            repository.observeReadoutOrder("lmu_windows").first(),
        )

        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage, ReadoutItemKey.VehicleApproach),
        )
        assertEquals(
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage, ReadoutItemKey.VehicleApproach),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `順序とenabledStatesは互いに独立して保存される`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleDamage, false)
        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
        )

        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(
                ReadoutItemKey.VehicleApproach to true,
                ReadoutItemKey.VehicleDamage to false,
            ),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
    }

    @Test
    fun `他シミュレータに順序があっても未保存のシミュレータは空リストを返す`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VehicleApproach))

        assertTrue(repository.observeReadoutOrder("rFactor 2").first().isEmpty())
    }

    @Test
    fun `未保存のシミュレータへの初回の順序保存はemptyListから開始され既存データを引き継がない`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag))
        repository.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.Flag))

        assertEquals(listOf(ReadoutItemKey.Flag), repository.observeReadoutOrder("rFactor 2").first())
    }

    @Test
    fun `シミュレーターごとに独立した順序を保存できる`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag))
        repository.saveReadoutOrder("rFactor 2", listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach))

        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
        assertEquals(
            listOf(ReadoutItemKey.Flag, ReadoutItemKey.VehicleApproach),
            repository.observeReadoutOrder("rFactor 2").first(),
        )
    }

    @Test
    fun `enabledState保存時に既存の順序が保持される`() = testScope.runTest {
        repository.saveReadoutOrder(
            "lmu_windows",
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
        )
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)

        assertEquals(
            listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag, ReadoutItemKey.VehicleDamage),
            repository.observeReadoutOrder("lmu_windows").first(),
        )
        assertEquals(
            mapOf<ReadoutItemKey, Boolean>(ReadoutItemKey.VehicleApproach to true),
            repository.observeReadoutEnabledStates("lmu_windows").first(),
        )
    }

    @Test
    fun `順序のみ保存済みのシミュレータはenabledStatesが空Mapを返す`() = testScope.runTest {
        repository.saveReadoutOrder("lmu_windows", listOf(ReadoutItemKey.VehicleApproach, ReadoutItemKey.Flag))

        assertTrue(repository.observeReadoutEnabledStates("lmu_windows").first().isEmpty())
    }

    @Test
    fun `enabledStateのみ保存済みのシミュレータはitemOrderが空リストを返す`() = testScope.runTest {
        repository.saveReadoutEnabledState("lmu_windows", ReadoutItemKey.VehicleApproach, true)

        assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())
    }
}
