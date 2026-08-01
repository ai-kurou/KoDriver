package kurou.kodriver.feature.acewindowsconnection

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
import kurou.kodriver.domain.usecase.ObserveAceWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

internal data class AceWindowsConnectionUiState(
    val connectionStatus: AceWindowsConnectionStatus = AceWindowsConnectionStatus.UNCHECKED,
    val fuelRemainingPercent: Double? = null,
) {
    val isConnected: Boolean get() = connectionStatus == AceWindowsConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean get() = connectionStatus != AceWindowsConnectionStatus.UNCHECKED
}

/**
 * AceWindowsConnection の接続状態を表す表示用ステータス。
 */
enum class AceWindowsConnectionStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}

internal class AceWindowsConnectionViewModel(
    private val observeAceWindowsConnection: ObserveAceWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<AceWindowsConnectionUiState> =
        observeSelectedSimulator()
        .flatMapLatest { simulator ->
            if (simulator is Simulator.AceWindows) {
                observeAceWindowsConnection().map { state ->
                    AceWindowsConnectionUiState(
                        connectionStatus =
                            if (state.isConnected) {
                            AceWindowsConnectionStatus.CONNECTED
                        } else {
                            AceWindowsConnectionStatus.DISCONNECTED
                        },
                            fuelRemainingPercent = state.fuel?.remainingPercent,
                    )
                }
            } else {
                flowOf(AceWindowsConnectionUiState())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = AceWindowsConnectionUiState(),
        )
}
