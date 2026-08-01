package kurou.kodriver.feature.gt7ps5connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.ObserveGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal data class Gt7Ps5ConnectionUiState(
    val connectionStatus: Gt7Ps5ConnectionStatus = Gt7Ps5ConnectionStatus.UNCHECKED,
    val fuelLevel: Float? = null,
    val fuelCapacity: Float? = null,
    val currentLap: Int? = null,
    val totalLaps: Int? = null,
) {
    val isConnected: Boolean get() = connectionStatus == Gt7Ps5ConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != Gt7Ps5ConnectionStatus.UNCHECKED
}

/**
 * Gt7Ps5Connection の接続状態を表す表示用ステータス。
 */
enum class Gt7Ps5ConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}

internal class Gt7Ps5ConnectionViewModel(
    private val observeGt7Ps5Connection: ObserveGt7Ps5ConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<Gt7Ps5ConnectionUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            if (simulator is Simulator.Gt7Ps5) {
                observeGt7Ps5Connection().map { state ->
                    Gt7Ps5ConnectionUiState(
                        connectionStatus = if (state.isConnected) {
                            Gt7Ps5ConnectionStatus.CONNECTED
                        } else {
                            Gt7Ps5ConnectionStatus.DISCONNECTED
                        },
                        fuelLevel = state.telemetry?.gasLevel,
                        fuelCapacity = state.telemetry?.gasCapacity,
                        currentLap = state.telemetry?.lapCount,
                        totalLaps = state.telemetry?.lapsInRace,
                    )
                }
            } else {
                flowOf(Gt7Ps5ConnectionUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Gt7Ps5ConnectionUiState(),
        )
}
