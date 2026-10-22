package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.ReadoutItemKeyRegistry
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.SimulatorReadoutSpec
import kurou.kodriver.domain.model.defaultAutoReadoutOrder
import kurou.kodriver.domain.repository.AutoReadoutPreferencesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveResolvedAutoReadoutOrderUseCaseTest {
    private data class TestReadoutItemKey(
        override val value: String,
    ) : ReadoutItemKey

    @BeforeTest
    fun registerSpecs() {
        val lmu = listOf("lmu_flag", "lmu_approach", "lmu_damage").map(::TestReadoutItemKey)
        val gt7 = listOf("gt7_best", "gt7_fuel", "gt7_laps", "gt7_tyre").map(::TestReadoutItemKey)
        val ace = listOf("ace_flag").map(::TestReadoutItemKey)
        listOf(
            SimulatorReadoutSpec(Simulator.LmuWindows, lmu, emptyMap(), emptyMap(), emptyMap(), lmu),
            SimulatorReadoutSpec(Simulator.Gt7Ps5, gt7, emptyMap(), emptyMap(), emptyMap(), gt7),
            SimulatorReadoutSpec(Simulator.AceWindows, ace, emptyMap(), emptyMap(), emptyMap(), ace),
        ).forEach { ReadoutItemKeyRegistry.register(it) }
    }

    private val repository = mockk<AutoReadoutPreferencesRepository>()
    private val useCase =
        ObserveResolvedAutoReadoutOrderUseCase(
            ObserveAutoReadoutOrderUseCase(repository),
            ResolveAutoReadoutOrderUseCase(),
        )

    @Test
    fun `未保存の場合はデフォルト順序を返す`() =
        runTest {
            every { repository.observeAutoReadoutOrder(Simulator.LmuWindows.id) } returns flowOf(emptyList())

            val result = useCase(Simulator.LmuWindows).first()

            assertEquals(defaultAutoReadoutOrder(Simulator.LmuWindows), result)
            assertEquals("lmu_flag", result.first().value)
            verify(exactly = 1) { repository.observeAutoReadoutOrder(Simulator.LmuWindows.id) }
            confirmVerified(repository)
        }

    @Test
    fun `保存済み順序を優先し新規項目を末尾に補完する`() =
        runTest {
            val persisted = listOf(TestReadoutItemKey("gt7_best"), TestReadoutItemKey("gt7_fuel"))
            every { repository.observeAutoReadoutOrder(Simulator.Gt7Ps5.id) } returns flowOf(persisted)

            val result = useCase(Simulator.Gt7Ps5).first()

            assertEquals(
                listOf(
                    TestReadoutItemKey("gt7_best"),
                    TestReadoutItemKey("gt7_fuel"),
                    TestReadoutItemKey("gt7_laps"),
                    TestReadoutItemKey("gt7_tyre"),
                ),
                result,
            )
            verify(exactly = 1) { repository.observeAutoReadoutOrder(Simulator.Gt7Ps5.id) }
            confirmVerified(repository)
        }

    @Test
    fun `LMUの保存済み順序にブレーキ摩耗がなければ末尾に補完する`() =
        runTest {
            val persisted = defaultAutoReadoutOrder(Simulator.LmuWindows).dropLast(1)
            every { repository.observeAutoReadoutOrder(Simulator.LmuWindows.id) } returns flowOf(persisted)

            val result = useCase(Simulator.LmuWindows).first()

            assertEquals(persisted + defaultAutoReadoutOrder(Simulator.LmuWindows).last(), result)
            verify(exactly = 1) { repository.observeAutoReadoutOrder(Simulator.LmuWindows.id) }
            confirmVerified(repository)
        }
}
