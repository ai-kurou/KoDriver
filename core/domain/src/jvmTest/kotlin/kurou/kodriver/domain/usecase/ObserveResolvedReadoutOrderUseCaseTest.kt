package kurou.kodriver.domain.usecase

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.defaultReadoutOrder
import kurou.kodriver.domain.repository.ReadoutPreferencesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

class ObserveResolvedReadoutOrderUseCaseTest {
    private val repository = mockk<ReadoutPreferencesRepository>()
    private val useCase =
        ObserveResolvedReadoutOrderUseCase(ObserveReadoutOrderUseCase(repository), ResolveReadoutOrderUseCase())

    @Test
    fun `未保存の場合はデフォルト順序を返す`() =
        runTest {
            every { repository.observeReadoutOrder(Simulator.LmuWindows.id) } returns flowOf(emptyList())

            val result = useCase(Simulator.LmuWindows).first()

            assertEquals(defaultReadoutOrder(Simulator.LmuWindows), result)
            assertEquals(ReadoutItemKey.LmuWindows.Flag.Root, result.first())
            verify(exactly = 1) { repository.observeReadoutOrder(Simulator.LmuWindows.id) }
            confirmVerified(repository)
        }

    @Test
    fun `保存済み順序を優先し新規項目を末尾に補完する`() =
        runTest {
            val persisted = listOf(ReadoutItemKey.Gt7Ps5.MyBestLap.Root, ReadoutItemKey.Gt7Ps5.RemainingFuel.Root)
            every { repository.observeReadoutOrder(Simulator.Gt7Ps5.id) } returns flowOf(persisted)

            val result = useCase(Simulator.Gt7Ps5).first()

            assertEquals(
                listOf(
                    ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    ReadoutItemKey.Gt7Ps5.RemainingFuel.Root,
                    ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                    ReadoutItemKey.Gt7Ps5.TyreTemperature.Root,
                ),
                result,
            )
            verify(exactly = 1) { repository.observeReadoutOrder(Simulator.Gt7Ps5.id) }
            confirmVerified(repository)
        }

    @Test
    fun `LMUの保存済み順序にブレーキ摩耗がなければ末尾に補完する`() =
        runTest {
            val persisted = defaultReadoutOrder(Simulator.LmuWindows) - ReadoutItemKey.LmuWindows.BrakeWear.Root
            every { repository.observeReadoutOrder(Simulator.LmuWindows.id) } returns flowOf(persisted)

            val result = useCase(Simulator.LmuWindows).first()

            assertEquals(persisted + ReadoutItemKey.LmuWindows.BrakeWear.Root, result)
            verify(exactly = 1) { repository.observeReadoutOrder(Simulator.LmuWindows.id) }
            confirmVerified(repository)
        }
}
