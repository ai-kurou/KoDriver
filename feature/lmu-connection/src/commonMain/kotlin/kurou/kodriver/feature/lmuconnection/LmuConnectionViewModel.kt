package kurou.kodriver.feature.lmuconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class LmuConnectionUiState(
    val isConnected: Boolean = false,
    val isConnectionChecked: Boolean = false,
)

class LmuConnectionViewModel(
    private val checkLmuConnection: CheckLmuConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuConnectionUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            if (simulator == LMU_SIMULATOR_KEY) {
                connectionCheckFlow()
            } else {
                flowOf(LmuConnectionUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LmuConnectionUiState(),
        )

    private fun connectionCheckFlow() = flow {
        while (true) {
            val isConnected = try {
                checkLmuConnection()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            emit(
                LmuConnectionUiState(
                    isConnected = isConnected,
                    isConnectionChecked = true,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
        const val LMU_SIMULATOR_KEY = "lmu_windows"
    }
}
