package kurou.kodriver.feature.acewindowsconnection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.acewindows.model.AceWindowsFuelData
import kurou.kodriver.domain.acewindows.usecase.AceWindowsConnectionState
import kurou.kodriver.domain.model.FuelPercent
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class AceWindowsConnectionFlowTest {
    @Test
    fun `ACE選択時は接続状態と燃料の更新を表示状態に反映する`() =
        runTest {
            val connection = MutableStateFlow(AceWindowsConnectionState(isConnected = false))
            val flow = AceWindowsConnectionFlow(flowOf(Simulator.AceWindows)) { connection }
            val states = mutableListOf<AceWindowsConnectionUiState>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                flow.uiState.collect { states += it }
            }
            runCurrent()
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.DISCONNECTED), states.last())

            connection.update {
                AceWindowsConnectionState(true, AceWindowsFuelData(FuelPercent(32.5)))
            }
            runCurrent()
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.CONNECTED, 32.5), states.last())

            connection.update { it.copy(fuel = AceWindowsFuelData(FuelPercent(28.0))) }
            runCurrent()
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.CONNECTED, 28.0), states.last())

            connection.update { it.copy(isConnected = false) }
            runCurrent()
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.DISCONNECTED, 28.0), states.last())

            connection.update { it.copy(fuel = null) }
            runCurrent()
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.DISCONNECTED), states.last())
        }

    @Test
    fun `ACE非選択時は接続Flowを生成せず初期状態を返す`() =
        runTest {
            for (simulator in listOf(Simulator.LmuWindows, Simulator.Gt7Ps5)) {
                val flow =
                    AceWindowsConnectionFlow(flowOf(simulator)) {
                        error("ACE非選択時は接続Flowを生成しない")
                    }

                assertEquals(AceWindowsConnectionUiState(), flow.uiState.first())
            }
        }

    @Test
    fun `選択切替で古い接続FlowをキャンセルしACE再選択時に新しく購読する`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.AceWindows)
            val oldConnection = MutableStateFlow(AceWindowsConnectionState(true, AceWindowsFuelData(FuelPercent(32.5))))
            val newConnection = MutableStateFlow(AceWindowsConnectionState(false))
            var observations = 0
            val flow =
                AceWindowsConnectionFlow(selectedSimulator) {
                    if (++observations == 1) oldConnection else newConnection
                }
            val states = mutableListOf<AceWindowsConnectionUiState>()
            assertEquals(0, observations)
            val job =
                backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    flow.uiState.collect { states += it }
                }
            runCurrent()
            assertEquals(1, observations)
            assertEquals(1, oldConnection.subscriptionCount.value)
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.CONNECTED, 32.5), states.last())

            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(0, oldConnection.subscriptionCount.value)
            assertEquals(AceWindowsConnectionUiState(), states.last())
            val count = states.size
            oldConnection.update { it.copy(isConnected = false) }
            runCurrent()
            assertEquals(count, states.size)

            selectedSimulator.update { Simulator.AceWindows }
            runCurrent()
            assertEquals(2, observations)
            assertEquals(1, newConnection.subscriptionCount.value)
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.DISCONNECTED), states.last())
            oldConnection.update { it.copy(isConnected = true) }
            runCurrent()
            assertEquals(AceWindowsConnectionUiState(AceWindowsConnectionStatus.DISCONNECTED), states.last())

            job.cancel()
            runCurrent()
            assertEquals(0, newConnection.subscriptionCount.value)
        }
}
