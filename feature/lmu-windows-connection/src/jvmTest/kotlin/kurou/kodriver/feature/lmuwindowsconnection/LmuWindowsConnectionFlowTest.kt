package kurou.kodriver.feature.lmuwindowsconnection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LmuWindowsConnectionFlowTest {
    @Test
    fun `LMU選択時は接続値を表示状態へ変換する`() =
        runTest {
            val flow =
                LmuWindowsConnectionFlow(
                    selectedSimulator = flowOf(Simulator.LmuWindows),
                    observeLmuWindowsConnection = { flowOf(true, false) },
                )

            assertEquals(
                listOf(
                    LmuWindowsConnectionUiState(LmuWindowsConnectionStatus.CONNECTED),
                    LmuWindowsConnectionUiState(LmuWindowsConnectionStatus.DISCONNECTED),
                ),
                flow.uiState.toList(),
            )
        }

    @Test
    fun `非LMU選択時は初期状態を返し接続監視を開始しない`() =
        runTest {
            for (simulator in listOf(Simulator.Gt7Ps5, Simulator.AceWindows)) {
                val flow =
                    LmuWindowsConnectionFlow(
                        selectedSimulator = flowOf(simulator),
                        observeLmuWindowsConnection = { error("LMU connection must not be observed") },
                    )

                assertEquals(LmuWindowsConnectionUiState(), flow.uiState.first())
            }
        }

    @Test
    fun `選択切替で旧接続FlowをキャンセルしLMU再選択で再購読する`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            var subscriptions = 0
            var cancellations = 0
            val flow =
                LmuWindowsConnectionFlow(
                    selectedSimulator = selectedSimulator,
                    observeLmuWindowsConnection = {
                        flow {
                            subscriptions++
                            try {
                                emit(subscriptions == 1)
                                awaitCancellation()
                            } finally {
                                cancellations++
                            }
                        }
                    },
                )
            val states = mutableListOf<LmuWindowsConnectionUiState>()
            val job = backgroundScope.launch { flow.uiState.collect { states.add(it) } }
            runCurrent()
            assertEquals(0, subscriptions)

            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(1, subscriptions)
            assertEquals(0, cancellations)

            selectedSimulator.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(1, cancellations)

            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(2, subscriptions)
            assertEquals(1, cancellations)
            assertEquals(
                listOf(
                    LmuWindowsConnectionUiState(),
                    LmuWindowsConnectionUiState(LmuWindowsConnectionStatus.CONNECTED),
                    LmuWindowsConnectionUiState(),
                    LmuWindowsConnectionUiState(LmuWindowsConnectionStatus.DISCONNECTED),
                ),
                states,
            )

            job.cancel()
            runCurrent()
            assertEquals(2, cancellations)
        }
}
