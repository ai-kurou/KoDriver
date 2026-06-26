package kurou.kodriver.feature.lmuwindowsconnection

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
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class LmuWindowsConnectionUiState(
    val connectionStatus: LmuWindowsConnectionStatus = LmuWindowsConnectionStatus.UNCHECKED,
) {
    val isConnected: Boolean get() = connectionStatus == LmuWindowsConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != LmuWindowsConnectionStatus.UNCHECKED
}

enum class LmuWindowsConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}

class LmuWindowsConnectionViewModel(
    private val checkLmuWindowsConnection: CheckLmuWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<LmuWindowsConnectionUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            if (simulator is Simulator.LmuWindows) {
                connectionCheckFlow()
            } else {
                flowOf(LmuWindowsConnectionUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LmuWindowsConnectionUiState(),
        )

    private fun connectionCheckFlow() = flow {
        while (true) {
            val isConnected = try {
                checkLmuWindowsConnection()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            emit(
                LmuWindowsConnectionUiState(
                    connectionStatus = if (isConnected) {
                        LmuWindowsConnectionStatus.CONNECTED
                    } else {
                        LmuWindowsConnectionStatus.DISCONNECTED
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
