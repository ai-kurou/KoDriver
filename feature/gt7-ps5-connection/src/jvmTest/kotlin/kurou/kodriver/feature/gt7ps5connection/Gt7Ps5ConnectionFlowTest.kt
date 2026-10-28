package kurou.kodriver.feature.gt7ps5connection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5FuelUnit
import kurou.kodriver.domain.gt7ps5.model.Gt7Ps5TelemetryData
import kurou.kodriver.domain.gt7ps5.usecase.Gt7Ps5ConnectionState
import kurou.kodriver.domain.model.Simulator
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class Gt7Ps5ConnectionFlowTest {
    private val telemetry =
        Gt7Ps5TelemetryData(
            lapCount = 4,
            lapsInRace = 12,
            bestLapTimeMs = 0,
            gasLevel = Gt7Ps5FuelUnit(32.5f),
            gasCapacity = Gt7Ps5FuelUnit(50f),
        )

    @Test
    fun `GT7選択時は接続状態と全テレメトリ項目の更新を反映する`() =
        runTest {
            val connection = MutableStateFlow(Gt7Ps5ConnectionState(isConnected = false))
            val flow = Gt7Ps5ConnectionFlow(flowOf(Simulator.Gt7Ps5)) { connection }
            val states = mutableListOf<Gt7Ps5ConnectionUiState>()
            backgroundScope.launch { flow.uiState.collect { states += it } }
            runCurrent()
            assertEquals(Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.DISCONNECTED), states.last())

            connection.update { Gt7Ps5ConnectionState(true, telemetry) }
            runCurrent()
            assertEquals(
                Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.CONNECTED, 32.5f, 50f, 4, 12),
                states.last(),
            )

            connection.update {
                it.copy(
                    telemetry =
                        telemetry.copy(
                            gasLevel = Gt7Ps5FuelUnit(28f),
                            gasCapacity = Gt7Ps5FuelUnit(60f),
                            lapCount = 5,
                            lapsInRace = 15,
                        ),
                )
            }
            runCurrent()
            assertEquals(
                Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.CONNECTED, 28f, 60f, 5, 15),
                states.last(),
            )

            connection.update { it.copy(isConnected = false) }
            runCurrent()
            assertEquals(
                Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.DISCONNECTED, 28f, 60f, 5, 15),
                states.last(),
            )

            connection.update { it.copy(isConnected = true, telemetry = null) }
            runCurrent()
            assertEquals(Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.CONNECTED), states.last())
        }

    @Test
    fun `GT7非選択時は接続Flowを生成せず初期状態を返す`() =
        runTest {
            for (simulator in listOf(Simulator.LmuWindows, Simulator.AceWindows)) {
                val flow =
                    Gt7Ps5ConnectionFlow(flowOf(simulator)) {
                        error("GT7非選択時は接続Flowを生成しない")
                    }

                assertEquals(Gt7Ps5ConnectionUiState(), flow.uiState.first())
            }
        }

    @Test
    fun `選択切替で旧接続FlowをキャンセルしGT7再選択で再購読する`() =
        runTest {
            val selectedSimulator = MutableStateFlow<Simulator>(Simulator.Gt7Ps5)
            val oldConnection = MutableStateFlow(Gt7Ps5ConnectionState(true, telemetry))
            val newConnection = MutableStateFlow(Gt7Ps5ConnectionState(false))
            var observations = 0
            val flow =
                Gt7Ps5ConnectionFlow(selectedSimulator) {
                    if (++observations == 1) oldConnection else newConnection
                }
            val states = mutableListOf<Gt7Ps5ConnectionUiState>()
            assertEquals(0, observations)
            assertEquals(0, selectedSimulator.subscriptionCount.value)
            val job = backgroundScope.launch { flow.uiState.collect { states += it } }
            runCurrent()
            assertEquals(1, observations)
            assertEquals(1, oldConnection.subscriptionCount.value)
            assertEquals(
                Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.CONNECTED, 32.5f, 50f, 4, 12),
                states.last(),
            )

            selectedSimulator.update { Simulator.LmuWindows }
            runCurrent()
            assertEquals(0, oldConnection.subscriptionCount.value)
            assertEquals(Gt7Ps5ConnectionUiState(), states.last())
            val count = states.size
            oldConnection.update { it.copy(isConnected = false) }
            runCurrent()
            assertEquals(count, states.size)

            selectedSimulator.update { Simulator.Gt7Ps5 }
            runCurrent()
            assertEquals(2, observations)
            assertEquals(1, newConnection.subscriptionCount.value)
            assertEquals(Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.DISCONNECTED), states.last())
            oldConnection.update { it.copy(isConnected = true) }
            runCurrent()
            assertEquals(Gt7Ps5ConnectionUiState(Gt7Ps5ConnectionStatus.DISCONNECTED), states.last())

            job.cancel()
            runCurrent()
            assertEquals(0, newConnection.subscriptionCount.value)
            assertEquals(0, selectedSimulator.subscriptionCount.value)
        }
}
