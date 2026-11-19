package kurou.kodriver.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DebugStateCardOrderFlowTest {
    private enum class TestKey { A, B, C }

    private val defaultOrder = listOf(TestKey.A, TestKey.B, TestKey.C)

    @Test
    fun `初期保存順を維持して不足カードを既定順で補完する`() =
        runTest {
            val persisted = listOf(TestKey.C, TestKey.B)
            val flow =
                DebugStateCardOrderFlow(
                    persistedOrder = flowOf(persisted),
                    localOrder = flowOf(null),
                    defaultOrder = defaultOrder,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(persisted + defaultOrder.filter { it !in persisted }, flow.order.first())
        }

    @Test
    fun `保存順が空なら既定順へフォールバックする`() =
        runTest {
            val flow =
                DebugStateCardOrderFlow(
                    persistedOrder = flowOf(emptyList()),
                    localOrder = flowOf(null),
                    defaultOrder = defaultOrder,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(defaultOrder, flow.order.first())
        }

    @Test
    fun `ローカル順を補完せず優先し空リストも維持する`() =
        runTest {
            val local = MutableStateFlow<List<TestKey>?>(listOf(TestKey.A))
            val flow =
                DebugStateCardOrderFlow(
                    persistedOrder = flowOf(defaultOrder.reversed()),
                    localOrder = local,
                    defaultOrder = defaultOrder,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(listOf(TestKey.A), flow.order.first())
            local.update { emptyList() }
            assertEquals(emptyList(), flow.order.first())
        }

    @Test
    fun `保存順の更新に追従しローカル解除時は最新保存順を解決する`() =
        runTest {
            val persisted = MutableStateFlow(defaultOrder)
            val local = MutableStateFlow<List<TestKey>?>(null)
            val flow =
                DebugStateCardOrderFlow(
                    persistedOrder = persisted,
                    localOrder = local,
                    defaultOrder = defaultOrder,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )
            val orders = mutableListOf<List<TestKey>>()
            backgroundScope.launch { flow.order.collect { orders.add(it) } }
            testScheduler.runCurrent()
            assertEquals(defaultOrder, orders.last())

            val reversed = defaultOrder.reversed()
            persisted.update { reversed }
            testScheduler.runCurrent()
            assertEquals(reversed, orders.last())

            val dragged = listOf(TestKey.A)
            local.update { dragged }
            testScheduler.runCurrent()
            assertEquals(dragged, orders.last())

            persisted.update { emptyList() }
            testScheduler.runCurrent()
            assertEquals(dragged, orders.last())

            local.update { null }
            testScheduler.runCurrent()
            assertEquals(defaultOrder, orders.last())
        }

    @Test
    fun `全件並べ替え済みの保存順をそのまま返す`() =
        runTest {
            val reversed = defaultOrder.reversed()
            val flow =
                DebugStateCardOrderFlow(
                    persistedOrder = flowOf(reversed),
                    localOrder = flowOf(null),
                    defaultOrder = defaultOrder,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(reversed, flow.order.first())
        }
}
