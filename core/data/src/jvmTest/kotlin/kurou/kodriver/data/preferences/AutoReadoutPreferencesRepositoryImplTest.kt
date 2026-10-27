package kurou.kodriver.data.preferences

import androidx.datastore.core.DataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kurou.kodriver.data.registerReadoutItemKeys
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKey
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class AutoReadoutPreferencesRepositoryImplTest {
    @BeforeTest
    fun `ReadoutItemKeyを登録する`() {
        registerReadoutItemKeys()
    }

    private val tempDir = Files.createTempDirectory("kodriver_readout_prefs_test").toFile()
    private val dataStoreScope = CoroutineScope(UnconfinedTestDispatcher())
    private val dataStore =
        DataStoreFactory.create(
            serializer = AutoReadoutPreferencesSerializer,
            scope = dataStoreScope,
            produceFile = { tempDir.resolve("test.pb") },
        )
    private val repository = AutoReadoutPreferencesRepositoryImpl(dataStore)

    @AfterTest
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun `初期値は空Map・保存した値を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeAutoReadoutEnabledStates("lmu_windows").first().isEmpty())

            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeAutoReadoutEnabledStates("lmu_windows").first(),
            )

            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, false)
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                repository.observeAutoReadoutEnabledStates("lmu_windows").first(),
            )
        }

    @Test
    fun `他シミュレーターにデータがあっても未保存のシミュレーターはemptyMapを返す`() =
        runTest {
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertTrue(repository.observeAutoReadoutEnabledStates("rFactor 2").first().isEmpty())
        }

    @Test
    fun `未保存のシミュレーターへの初回保存はemptyMapから開始され既存データを引き継がない`() =
        runTest {
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveAutoReadoutEnabledState("rFactor 2", LmuWindowsReadoutItemKey.Flag.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.Flag.Root to false),
                repository.observeAutoReadoutEnabledStates("rFactor 2").first(),
            )
        }

    @Test
    fun `複数アイテムを独立して保存・取得できる`() =
        runTest {
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.Flag.Root, false)
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleDamage.Root, true)
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.TyreTemperature.Root, false)

            val states = repository.observeAutoReadoutEnabledStates("lmu_windows").first()
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
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveAutoReadoutEnabledState("rFactor 2", LmuWindowsReadoutItemKey.VehicleApproach.Root, false)

            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeAutoReadoutEnabledStates("lmu_windows").first(),
            )
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to false),
                repository.observeAutoReadoutEnabledStates("rFactor 2").first(),
            )
        }

    @Test
    fun `順序の初期値は空リスト・保存した順序を返す・上書きで更新される`() =
        runTest {
            assertTrue(repository.observeAutoReadoutOrder("lmu_windows").first().isEmpty())

            repository.saveAutoReadoutOrder(
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
                repository.observeAutoReadoutOrder("lmu_windows").first(),
            )

            repository.saveAutoReadoutOrder(
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
                repository.observeAutoReadoutOrder("lmu_windows").first(),
            )
        }

    @Test
    fun `順序とenabledStatesは互いに独立して保存される`() =
        runTest {
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleDamage.Root, false)
            repository.saveAutoReadoutOrder(
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
                repository.observeAutoReadoutEnabledStates("lmu_windows").first(),
            )
            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
                repository.observeAutoReadoutOrder("lmu_windows").first(),
            )
        }

    @Test
    fun `他シミュレーターに順序があっても未保存のシミュレーターは空リストを返す`() =
        runTest {
            repository.saveAutoReadoutOrder("lmu_windows", listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root))

            assertTrue(repository.observeAutoReadoutOrder("rFactor 2").first().isEmpty())
        }

    @Test
    fun `未保存のシミュレーターへの初回の順序保存はemptyListから開始され既存データを引き継がない`() =
        runTest {
            repository.saveAutoReadoutOrder(
                "lmu_windows",
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
            )
            repository.saveAutoReadoutOrder("rFactor 2", listOf(LmuWindowsReadoutItemKey.Flag.Root))

            assertEquals(
                listOf(LmuWindowsReadoutItemKey.Flag.Root),
                repository.observeAutoReadoutOrder("rFactor 2").first(),
            )
        }

    @Test
    fun `シミュレーターごとに独立した順序を保存できる`() =
        runTest {
            repository.saveAutoReadoutOrder(
                "lmu_windows",
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
            )
            repository.saveAutoReadoutOrder(
                "rFactor 2",
                listOf(LmuWindowsReadoutItemKey.Flag.Root, LmuWindowsReadoutItemKey.VehicleApproach.Root),
            )

            assertEquals(
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
                repository.observeAutoReadoutOrder("lmu_windows").first(),
            )
            assertEquals(
                listOf(LmuWindowsReadoutItemKey.Flag.Root, LmuWindowsReadoutItemKey.VehicleApproach.Root),
                repository.observeAutoReadoutOrder("rFactor 2").first(),
            )
        }

    @Test
    fun `enabledState保存時に既存の順序が保持される`() =
        runTest {
            repository.saveAutoReadoutOrder(
                "lmu_windows",
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
            )
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertEquals(
                listOf(
                    LmuWindowsReadoutItemKey.VehicleApproach.Root,
                    LmuWindowsReadoutItemKey.Flag.Root,
                    LmuWindowsReadoutItemKey.VehicleDamage.Root,
                ),
                repository.observeAutoReadoutOrder("lmu_windows").first(),
            )
            assertEquals(
                mapOf<ReadoutItemKey, Boolean>(LmuWindowsReadoutItemKey.VehicleApproach.Root to true),
                repository.observeAutoReadoutEnabledStates("lmu_windows").first(),
            )
        }

    @Test
    fun `順序のみ保存済みのシミュレーターはenabledStatesが空Mapを返す`() =
        runTest {
            repository.saveAutoReadoutOrder(
                "lmu_windows",
                listOf(LmuWindowsReadoutItemKey.VehicleApproach.Root, LmuWindowsReadoutItemKey.Flag.Root),
            )

            assertTrue(repository.observeAutoReadoutEnabledStates("lmu_windows").first().isEmpty())
        }

    @Test
    fun `enabledStateのみ保存済みのシミュレーターはitemOrderが空リストを返す`() =
        runTest {
            repository.saveAutoReadoutEnabledState("lmu_windows", LmuWindowsReadoutItemKey.VehicleApproach.Root, true)

            assertTrue(repository.observeAutoReadoutOrder("lmu_windows").first().isEmpty())
        }

    @Test
    fun `全シミュレーターの有効状態と順序を既定値に戻す`() =
        runTest {
            val simulators = listOf("lmu_windows", "ace_windows", "gt7_ps5")
            simulators.forEach { simulator ->
                repository.saveAutoReadoutEnabledState(simulator, LmuWindowsReadoutItemKey.Flag.Root, false)
                repository.saveAutoReadoutOrder(simulator, listOf(LmuWindowsReadoutItemKey.Flag.Root))
            }
            assertNotEquals(AutoReadoutPreferencesSerializer.defaultValue, dataStore.data.first())

            repository.resetToDefault()

            assertEquals(AutoReadoutPreferencesSerializer.defaultValue, dataStore.data.first())
            simulators.forEach { simulator ->
                assertEquals(emptyMap(), repository.observeAutoReadoutEnabledStates(simulator).first())
                assertEquals(emptyList(), repository.observeAutoReadoutOrder(simulator).first())
            }
        }
}
