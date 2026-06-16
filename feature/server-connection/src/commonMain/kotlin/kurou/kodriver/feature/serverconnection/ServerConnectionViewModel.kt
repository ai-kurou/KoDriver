package kurou.kodriver.feature.serverconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.CheckServerConnectionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

data class ServerConnectionUiState(
    val isConnected: Boolean = false,
    val isConnectionChecked: Boolean = false,
    val isIpConfigured: Boolean = false,
    val requiresKoDriverServer: Boolean = false,
    val selectedSimulator: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConnectionViewModel(
    private val checkServerConnection: CheckServerConnectionUseCase,
    private val observeServerIp: ObserveServerIpUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
) : ViewModel() {

    val uiState: StateFlow<ServerConnectionUiState> = combine(
        observeServerIp(),
        observeSelectedSimulator(),
    ) { ip, simulator -> ip to simulator }
        .flatMapLatest { (ip, simulator) ->
            val requiresServer = Simulator.fromId(simulator.orEmpty())?.requiresKoDriverServer == true
            if (ip != null) {
                connectionCheckFlow(ip, simulator, requiresServer)
            } else {
                flowOf(
                    ServerConnectionUiState(
                        isIpConfigured = false,
                        requiresKoDriverServer = requiresServer,
                        selectedSimulator = simulator,
                    ),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = ServerConnectionUiState(),
        )

    private fun connectionCheckFlow(ip: String, simulator: String?, requiresServer: Boolean) = flow {
        while (true) {
            val isConnected = try {
                checkServerConnection(ip)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            emit(
                ServerConnectionUiState(
                    isConnected = isConnected,
                    isConnectionChecked = true,
                    isIpConfigured = true,
                    requiresKoDriverServer = requiresServer,
                    selectedSimulator = simulator,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
