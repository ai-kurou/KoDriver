package kurou.kodriver.feature.acewindowsdebugstatedetail

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.usecase.ResolveDebugStateCardOrderUseCase
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsDebugStateCardOrderFlowTest {
    @Test
    fun `初期保存順を優先し不足カードを既定順で補完する`() =
        runTest {
            val persisted = listOf(AceWindowsDebugStateCardKey.BEST_LAP)
            val flow =
                AceWindowsDebugStateCardOrderFlow(
                    persistedOrder = MutableStateFlow(persisted),
                    localOrder = MutableStateFlow(null),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(
                persisted + defaultDebugStateCardOrder.filter { it !in persisted },
                flow.cardOrder.first(),
            )
        }

    @Test
    fun `空の初期保存順は既定順にフォールバックする`() =
        runTest {
            val flow =
                AceWindowsDebugStateCardOrderFlow(
                    persistedOrder = MutableStateFlow(emptyList()),
                    localOrder = MutableStateFlow(null),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )

            assertEquals(defaultDebugStateCardOrder, flow.cardOrder.first())
        }

    @Test
    fun `ローカル順がない間は保存順の更新に追従する`() =
        runTest {
            val persisted = MutableStateFlow(defaultDebugStateCardOrder)
            val flow =
                AceWindowsDebugStateCardOrderFlow(
                    persistedOrder = persisted,
                    localOrder = MutableStateFlow(null),
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )
            val orders = mutableListOf<List<AceWindowsDebugStateCardKey>>()
            backgroundScope.launch { flow.cardOrder.collect { orders.add(it) } }
            testScheduler.runCurrent()
            assertEquals(defaultDebugStateCardOrder, orders.last())

            persisted.update { defaultDebugStateCardOrder.reversed() }
            testScheduler.runCurrent()

            assertEquals(defaultDebugStateCardOrder.reversed(), orders.last())
        }

    @Test
    fun `ローカル順を補完せず優先しnullに戻ると最新の保存順を解決する`() =
        runTest {
            val persisted = MutableStateFlow(defaultDebugStateCardOrder)
            val local = MutableStateFlow<List<AceWindowsDebugStateCardKey>?>(null)
            val flow =
                AceWindowsDebugStateCardOrderFlow(
                    persistedOrder = persisted,
                    localOrder = local,
                    resolveCardOrder = ResolveDebugStateCardOrderUseCase(),
                )
            val orders = mutableListOf<List<AceWindowsDebugStateCardKey>>()
            backgroundScope.launch { flow.cardOrder.collect { orders.add(it) } }
            testScheduler.runCurrent()

            val localOrder = listOf(AceWindowsDebugStateCardKey.FLAG_INFO)
            local.update { localOrder }
            testScheduler.runCurrent()
            assertEquals(localOrder, orders.last())

            val savedOrder = listOf(AceWindowsDebugStateCardKey.BEST_LAP)
            persisted.update { savedOrder }
            testScheduler.runCurrent()
            assertEquals(localOrder, orders.last())

            local.update { null }
            testScheduler.runCurrent()
            assertEquals(
                savedOrder + defaultDebugStateCardOrder.filter { it !in savedOrder },
                orders.last(),
            )
        }
}
