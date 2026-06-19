package kurou.kodriver.feature.serverconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.FetchServerVersionUseCase
import kurou.kodriver.domain.usecase.ObserveSelectedSimulatorUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

enum class ServerConnectionStatus { NOT_CONFIGURED, CHECKING, CONNECTED, DISCONNECTED }

data class ServerConnectionUiState(
    val connectionStatus: ServerConnectionStatus = ServerConnectionStatus.NOT_CONFIGURED,
    val requiresKoDriverServer: Boolean = false,
    val selectedSimulator: String? = null,
    val serverVersion: String? = null,
    val showVersionMismatchBottomSheet: Boolean = false,
    val appVersion: String = "",
) {
    val isConnected: Boolean get() = connectionStatus == ServerConnectionStatus.CONNECTED
    val isConnectionChecked: Boolean
        get() = connectionStatus != ServerConnectionStatus.NOT_CONFIGURED &&
            connectionStatus != ServerConnectionStatus.CHECKING
    val isIpConfigured: Boolean get() = connectionStatus != ServerConnectionStatus.NOT_CONFIGURED
}

@OptIn(ExperimentalCoroutinesApi::class)
class ServerConnectionViewModel(
    private val fetchServerVersion: FetchServerVersionUseCase,
    private val observeServerIp: ObserveServerIpUseCase,
    private val observeSelectedSimulator: ObserveSelectedSimulatorUseCase,
    private val appVersion: String,
) : ViewModel() {

    private val _showVersionMismatchBottomSheet = MutableStateFlow(false)
    private var versionMismatchWarningShown = false

    private val baseUiStateFlow = combine(
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
                        connectionStatus = ServerConnectionStatus.NOT_CONFIGURED,
                        requiresKoDriverServer = requiresServer,
                        selectedSimulator = simulator,
                    ),
                )
            }
        }

    val uiState: StateFlow<ServerConnectionUiState> = combine(
        baseUiStateFlow,
        _showVersionMismatchBottomSheet,
    ) { base, showBottomSheet ->
        base.copy(showVersionMismatchBottomSheet = showBottomSheet, appVersion = appVersion)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ServerConnectionUiState(),
    )

    fun dismissVersionMismatchBottomSheet() {
        _showVersionMismatchBottomSheet.value = false
    }

    private fun connectionCheckFlow(ip: String, simulator: String?, requiresServer: Boolean) = flow {
        emit(
            ServerConnectionUiState(
                connectionStatus = ServerConnectionStatus.CHECKING,
                requiresKoDriverServer = requiresServer,
                selectedSimulator = simulator,
            ),
        )
        while (true) {
            val versionResult = fetchServerVersion(ip)
            val serverVer = versionResult.getOrNull()
            val isMismatch = serverVer != null && appVersion.isNotEmpty() && serverVer != appVersion
            if (isMismatch && !versionMismatchWarningShown) {
                versionMismatchWarningShown = true
                _showVersionMismatchBottomSheet.value = true
            }
            emit(
                ServerConnectionUiState(
                    connectionStatus = if (versionResult.isSuccess) {
                        ServerConnectionStatus.CONNECTED
                    } else {
                        ServerConnectionStatus.DISCONNECTED
                    },
                    requiresKoDriverServer = requiresServer,
                    selectedSimulator = simulator,
                    serverVersion = serverVer,
                ),
            )
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
