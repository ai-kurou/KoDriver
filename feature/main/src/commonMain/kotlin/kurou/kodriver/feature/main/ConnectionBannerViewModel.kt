package kurou.kodriver.feature.main

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
import kurou.kodriver.domain.usecase.CheckLmuWindowsConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class ConnectionBannerVmUiState(
    val connectionStatus: ConnectionBannerVmStatus = ConnectionBannerVmStatus.UNCHECKED,
)

enum class ConnectionBannerVmStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
}

class ConnectionBannerViewModel(
    private val checkLmuWindowsConnection: CheckLmuWindowsConnectionUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ConnectionBannerVmUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            when (simulator) {
                LMU_WINDOWS_SIMULATOR_KEY -> lmuConnectionCheckFlow()
                else -> flowOf(ConnectionBannerVmUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ConnectionBannerVmUiState(),
        )

    private fun lmuConnectionCheckFlow() = flow {
        while (true) {
            val isConnected = try {
                checkLmuWindowsConnection()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            emit(
                ConnectionBannerVmUiState(
                    connectionStatus = if (isConnected) {
                        ConnectionBannerVmStatus.CONNECTED
                    } else {
                        ConnectionBannerVmStatus.DISCONNECTED
                    },
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
