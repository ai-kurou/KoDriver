package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadoutPreferencesRepositoryImplTest {
    private val tempDir = Files.createTempDirectory("kodriver_readout_prefs_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = ReadoutPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = ReadoutPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeReadoutEnabledStates("lmu_windows").first().isEmpty())

            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeReadoutEnabledStates("lmu_windows").first(),
            )

            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                repository.observeReadoutEnabledStates("lmu_windows").first(),
            )
        }

    @Test
    fun `他シミュレータにデータがあっても未保存のシミュレータはemptyMapを返す`() =
        runTest {
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertTrue(repository.observeReadoutEnabledStates("rFactor 2").first().isEmpty())
        }

    @Test
    fun `未保存のシミュレータへの初回保存はemptyMapから開始され既存データを引き継がない`() =
        runTest {
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveReadoutEnabledState("rFactor 2", LmuWindowsReadoutItemKey.Flag.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to false),
                repository.observeReadoutEnabledStates("rFactor 2").first(),
            )
        }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() =
        runTest {
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.Flag.Root, false)
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleDamage.Root, true)
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.TyreTemperature.Root, false)

            val states = repository.observeReadoutEnabledStates("lmu_windows").first()
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
                    LmuWindowsReadoutItemKey.Flag.Root to false,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to true,
                    LmuWindowsReadoutItemKey.TyreTemperature.Root to false,
                ),
                states,
            )
        }

    @Test
    fun `シミュレーターごとに独立した状態を保存できる`() =
        runTest {
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveReadoutEnabledState("rFactor 2", LmuWindowsReadoutItemKey.VehicleApproach.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeReadoutEnabledStates("lmu_windows").first(),
            )
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                repository.observeReadoutEnabledStates("rFactor 2").first(),
            )
        }

    @Test
    fun `順序の初期値は空リスト・保存した順序を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())

            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
            )
            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
                repository.observeReadoutOrder("lmu_windows").first(),
            )

            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                ),
            )
            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                ),
                repository.observeReadoutOrder("lmu_windows").first(),
            )
        }

    @Test
    fun `順序とenabledStatesは互いに独立して保存される`() =
        runTest {
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleDamage.Root, false)
            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
            )

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root to true,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root to false,
                ),
                repository.observeReadoutEnabledStates("lmu_windows").first(),
            )
            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
                repository.observeReadoutOrder("lmu_windows").first(),
            )
        }

    @Test
    fun `他シミュレータに順序があっても未保存のシミュレータは空リストを返す`() =
        runTest {
            repository.saveReadoutOrder("lmu_windows", listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root))

            assertTrue(repository.observeReadoutOrder("rFactor 2").first().isEmpty())
        }

    @Test
    fun `未保存のシミュレータへの初回の順序保存はemptyListから開始され既存データを引き継がない`() =
        runTest {
            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
            )
            repository.saveReadoutOrder("rFactor 2", listOf(LmuWindowsReadoutItemKey.Flag.Root))

            assertEquals(
                listOf(LmuWindowsReadoutItemKey.Flag.Root),
                repository.observeReadoutOrder("rFactor 2").first(),
            )
        }

    @Test
    fun `シミュレーターごとに独立した順序を保存できる`() =
        runTest {
            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
            )
            repository.saveReadoutOrder(
                "rFactor 2",
                listOf(LmuWindowsReadoutItemKey.Flag.Root, LmuWindowsReadoutItemKey.VehicleApproach.Root),
            )

            assertEquals(
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
                repository.observeReadoutOrder("lmu_windows").first(),
            )
            assertEquals(
                listOf(LmuWindowsReadoutItemKey.Flag.Root, LmuWindowsReadoutItemKey.VehicleApproach.Root),
                repository.observeReadoutOrder("rFactor 2").first(),
            )
        }

    @Test
    fun `enabledState保存時に既存の順序が保持される`() =
        runTest {
            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
            )
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
                repository.observeReadoutOrder("lmu_windows").first(),
            )
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeReadoutEnabledStates("lmu_windows").first(),
            )
        }

    @Test
    fun `順序のみ保存済みのシミュレータはenabledStatesが空Mapを返す`() =
        runTest {
            repository.saveReadoutOrder(
                "lmu_windows",
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
            )

            assertTrue(repository.observeReadoutEnabledStates("lmu_windows").first().isEmpty())
        }

    @Test
    fun `enabledStateのみ保存済みのシミュレータはitemOrderが空リストを返す`() =
        runTest {
            repository.saveReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertTrue(repository.observeReadoutOrder("lmu_windows").first().isEmpty())
        }
}
