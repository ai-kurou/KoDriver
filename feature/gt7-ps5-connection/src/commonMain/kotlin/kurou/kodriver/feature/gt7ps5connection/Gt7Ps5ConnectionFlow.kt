package kurou.kodriver.feature.gt7ps5connection

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kurou.kodriver.domain.gt7ps5.usecase.Gt7Ps5ConnectionState
import kurou.kodriver.domain.model.Simulator

/** 選択中のシミュレーターに応じて GT7 の接続状態を表示状態へ変換する冷たい Flow。 */
internal class Gt7Ps5ConnectionFlow(
    selectedSimulator: Flow<Simulator>,
    private val observeGt7Ps5Connection: () -> Flow<Gt7Ps5ConnectionState>,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: Flow<Gt7Ps5ConnectionUiState> =
        selectedSimulator.flatMapLatest { simulator ->
            if (simulator is Simulator.Gt7Ps5) {
                observeGt7Ps5Connection().map { state ->
                    Gt7Ps5ConnectionUiState(
                        connectionStatus =
                            if (state.isConnected) {
                                Gt7Ps5ConnectionStatus.CONNECTED
                            } else {
                                Gt7Ps5ConnectionStatus.DISCONNECTED
                            },
                        fuelLevel = state.telemetry?.gasLevel?.value,
                        fuelCapacity = state.telemetry?.gasCapacity?.value,
                        currentLap = state.telemetry?.lapCount,
                        totalLaps = state.telemetry?.lapsInRace,
                    )
                }
            } else {
                flowOf(Gt7Ps5ConnectionUiState())
            }
        }
}
