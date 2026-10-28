package kurou.kodriver.feature.gt7ps5debugstatedetail

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
class Gt7Ps5DebugStateCardOrderFlowTest {
    @Test
    fun `初期保存順を復元し空や不足する保存順を補完する`() =
        runTest {
            val reversed = defaultDebugStateCardOrder.reversed()
            val partial = listOf(Gt7Ps5DebugStateCardKey.BEST_LAP)
            val cases =
                listOf(
                    reversed to reversed,
                    emptyList<Gt7Ps5DebugStateCardKey>() to defaultDebugStateCardOrder,
                    partial to (partial + defaultDebugStateCardOrder.filter { it !in partial }),
                )

            for ((persisted, expected) in cases) {
                val flow =
                    Gt7Ps5DebugStateCardOrderFlow(
                        persistedOrder = flowOf(persisted),
                        localOrder = flowOf(null),
                        resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                    )

                assertEquals(expected, flow.order.first())
            }
        }

    @Test
    fun `ローカル順を優先しnullに戻ると最新の保存順を解決する`() =
        runTest {
            val persisted = MutableStateFlow(defaultDebugStateCardOrder)
            val local = MutableStateFlow<List<Gt7Ps5DebugStateCardKey>?>(defaultDebugStateCardOrder.reversed())
            val flow = Gt7Ps5DebugStateCardOrderFlow(persisted, local, ResolveDebugStateCardOrderUseCase())
            val orders = mutableListOf<List<Gt7Ps5DebugStateCardKey>>()
            backgroundScope.launch { flow.order.collect { orders.add(it) } }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder.reversed(), orders.last())

            val saved = listOf(Gt7Ps5DebugStateCardKey.BEST_LAP)
            persisted.update { saved }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder.reversed(), orders.last())

            local.update { emptyList() }
            testScheduler.runCurrent()
            assertEquals(emptyList(), orders.last())

            local.update { null }
            testScheduler.runCurrent()
            assertEquals(saved + defaultDebugStateCardOrder.filter { it !in saved }, orders.last())
        }

    @Test
    fun `ローカル順がnullなら保存順の更新に追従する`() =
        runTest {
            val persisted = MutableStateFlow(defaultDebugStateCardOrder)
            val flow =
                Gt7Ps5DebugStateCardOrderFlow(
                    persistedOrder = persisted,
                    localOrder = flowOf(null),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )
            val orders = mutableListOf<List<Gt7Ps5DebugStateCardKey>>()
            backgroundScope.launch { flow.order.collect { orders.add(it) } }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder, orders.last())

            persisted.update { defaultDebugStateCardOrder.reversed() }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder.reversed(), orders.last())

            persisted.update { emptyList() }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder, orders.last())
        }
}
