package kurou.kodriver.feature.gt7ps5connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class Gt7Ps5ConnectionUiState(
    val connectionStatus: Gt7Ps5ConnectionStatus = Gt7Ps5ConnectionStatus.UNCHECKED,
) {
    val isConnected: Boolean get() = connectionStatus == Gt7Ps5ConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != Gt7Ps5ConnectionStatus.UNCHECKED
}

enum class Gt7Ps5ConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}

class Gt7Ps5ConnectionViewModel(
    private val checkGt7Ps5Connection: CheckGt7Ps5ConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<Gt7Ps5ConnectionUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            if (simulator is Simulator.Gt7Ps5) {
                connectionCheckFlow()
            } else {
                flowOf(Gt7Ps5ConnectionUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = Gt7Ps5ConnectionUiState(),
        )

    private fun connectionCheckFlow() = flow {
        while (true) {
            val isConnected = try {
                checkGt7Ps5Connection()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            emit(
                Gt7Ps5ConnectionUiState(
                    connectionStatus = if (isConnected) {
                        Gt7Ps5ConnectionStatus.CONNECTED
                    } else {
                        Gt7Ps5ConnectionStatus.DISCONNECTED
                    },
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
