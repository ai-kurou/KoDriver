package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsDebugStateCardOrderFlowTest {
    @Test
    fun `初期保存順を維持して不足カードを既定順で補完する`() =
        runTest {
            val persisted = listOf(LmuWindowsDebugStateCardKey.CAR_GAP, LmuWindowsDebugStateCardKey.FUEL_CONSUMPTION)
            val flow =
                LmuWindowsDebugStateCardOrderFlow(
                    persistedOrder = flowOf(persisted),
                    localOrder = flowOf(null),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(persisted + defaultDebugStateCardOrder.filter { it !in persisted }, flow.order.first())
        }

    @Test
    fun `保存順が空なら既定順へフォールバックする`() =
        runTest {
            val flow =
                LmuWindowsDebugStateCardOrderFlow(
                    persistedOrder = flowOf(emptyList()),
                    localOrder = flowOf(null),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(defaultDebugStateCardOrder, flow.order.first())
        }

    @Test
    fun `ローカル順を補完せず優先し空リストも維持する`() =
        runTest {
            val local =
                MutableStateFlow<List<LmuWindowsDebugStateCardKey>?>(listOf(LmuWindowsDebugStateCardKey.CAR_GAP))
            val flow =
                LmuWindowsDebugStateCardOrderFlow(
                    persistedOrder = flowOf(defaultDebugStateCardOrder.reversed()),
                    localOrder = local,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(listOf(LmuWindowsDebugStateCardKey.CAR_GAP), flow.order.first())
            local.update { emptyList() }
            assertEquals(emptyList(), flow.order.first())
        }

    @Test
    fun `保存順の更新に追従しローカル解除時は最新保存順を解決する`() =
        runTest {
            val persisted = MutableStateFlow(defaultDebugStateCardOrder)
            val local = MutableStateFlow<List<LmuWindowsDebugStateCardKey>?>(null)
            val flow =
                LmuWindowsDebugStateCardOrderFlow(
                    persistedOrder = persisted,
                    localOrder = local,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )
            val orders = mutableListOf<List<LmuWindowsDebugStateCardKey>>()
            backgroundScope.launch { flow.order.collect { orders.add(it) } }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder, orders.last())

            val reversed = defaultDebugStateCardOrder.reversed()
            persisted.update { reversed }
            testScheduler.runCurrent()
            assertEquals(reversed, orders.last())

            val dragged = listOf(LmuWindowsDebugStateCardKey.CAR_GAP)
            local.update { dragged }
            testScheduler.runCurrent()
            assertEquals(dragged, orders.last())

            persisted.update { emptyList() }
            testScheduler.runCurrent()
            assertEquals(dragged, orders.last())

            local.update { null }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder, orders.last())
        }
}
