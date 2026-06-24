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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.CheckGt7Ps5ConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase

data class ConnectionBannerVmUiState(
    val connectionStatus: ConnectionBannerVmStatus = ConnectionBannerVmStatus.UNCHECKED,
    val selectedSimulator: String? = null,
)

enum class ConnectionBannerVmStatus {
    UNCHECKED,
    CONNECTED,
    DISCONNECTED,
    IP_NOT_CONFIGURED,
}

class ConnectionBannerViewModel(
    private val checkLmuConnection: LmuBannerConnectionChecker,
    private val checkGt7Ps5Connection: CheckGt7Ps5ConnectionUseCase,
    private val observeConsoleAddress: ObserveConsoleAddressUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ConnectionBannerVmUiState> = observeSelectedSimulator()
        .flatMapLatest { simulator ->
            when (simulator) {
                LMU_WINDOWS_SIMULATOR_KEY -> checkLmuConnection.statusFlow()
                    .map { ConnectionBannerVmUiState(it, simulator) }
                GT7_PS5_SIMULATOR_KEY -> gt7ConnectionFlow(simulator)
                else -> flowOf(ConnectionBannerVmUiState())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ConnectionBannerVmUiState(),
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun gt7ConnectionFlow(simulator: String) = observeConsoleAddress()
        .flatMapLatest { address ->
            if (address == null) {
                flowOf(ConnectionBannerVmUiState(ConnectionBannerVmStatus.IP_NOT_CONFIGURED, simulator))
            } else {
                connectionCheckFlow(simulator) { checkGt7Ps5Connection() }
            }
        }

    private fun connectionCheckFlow(simulator: String, check: suspend () -> Boolean) = flow {
        while (true) {
            val isConnected = try {
                check()
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
                    selectedSimulator = simulator,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
        const val LMU_WINDOWS_SIMULATOR_KEY = "lmu_windows"
        const val GT7_PS5_SIMULATOR_KEY = "gt7_ps5"
    }
}
