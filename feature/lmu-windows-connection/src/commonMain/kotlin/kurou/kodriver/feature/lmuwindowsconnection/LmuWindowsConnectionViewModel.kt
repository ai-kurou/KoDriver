package kurou.kodriver.feature.lmuwindowsconnection

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
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class LmuWindowsConnectionUiState(
    val isConnected: Boolean = false,
    val isConnectionChecked: Boolean = false,
)

class LmuWindowsConnectionViewModel(
    private val checkLmuWindowsConnection: CheckLmuWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuWindowsConnectionUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            if (simulator == LMU_WINDOWS_SIMULATOR_KEY) {
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
                    isConnected = isConnected,
                    isConnectionChecked = true,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
        const val LMU_WINDOWS_SIMULATOR_KEY = "lmu_windows"
    }
}
