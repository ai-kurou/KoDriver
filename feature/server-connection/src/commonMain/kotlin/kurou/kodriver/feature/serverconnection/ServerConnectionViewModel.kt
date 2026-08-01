package kurou.kodriver.feature.serverconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.usecase.KoDriverServerConnectionStatus
import kurou.kodriver.domain.usecase.ObserveKoDriverServerConnectionUseCase

/**
 * ServerConnection の接続状態を表す表示用ステータス。
 */
enum class ServerConnectionStatus { NOT_CONFIGURED, CHECKING, CONNECTED, DISCONNECTED }

/**
 * ServerConnection 画面の表示状態。
 */
data class ServerConnectionUiState(
    val connectionStatus: ServerConnectionStatus = ServerConnectionStatus.NOT_CONFIGURED,
    val requiresKoDriverServer: Boolean = false,
    val selectedSimulator: Simulator? = null,
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

/**
 * ServerConnection 画面の状態管理とユーザー操作を扱う ViewModel。
 */
class ServerConnectionViewModel(
    observeKoDriverServerConnection: ObserveKoDriverServerConnectionUseCase,
    private val appVersion: String,
) : ViewModel() {

    private val showVersionMismatchBottomSheetFlow = MutableStateFlow(false)
    private var versionMismatchWarningShown = false

    private val connectionStateFlow = observeKoDriverServerConnection(appVersion)
        .onEach { state ->
            if (state.isVersionMismatch && !versionMismatchWarningShown) {
                versionMismatchWarningShown = true
                showVersionMismatchBottomSheetFlow.update { true }
            }
        }.shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            replay = 1,
        )

    private val baseUiStateFlow = connectionStateFlow
        .map { state ->
            ServerConnectionUiState(
                connectionStatus = state.connectionStatus.toUiStatus(),
                requiresKoDriverServer = state.requiresKoDriverServer,
                selectedSimulator = state.selectedSimulator,
                serverVersion = state.serverVersion,
            )
        }

    val uiState: StateFlow<ServerConnectionUiState> = combine(
        baseUiStateFlow,
        showVersionMismatchBottomSheetFlow,
    ) { base, showBottomSheet ->
        base.copy(showVersionMismatchBottomSheet = showBottomSheet, appVersion = appVersion)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = ServerConnectionUiState(),
    )

    fun dismissVersionMismatchBottomSheet() {
        showVersionMismatchBottomSheetFlow.update { false }
    }
}

private fun KoDriverServerConnectionStatus.toUiStatus(): ServerConnectionStatus = when (this) {
    KoDriverServerConnectionStatus.NOT_CONFIGURED -> ServerConnectionStatus.NOT_CONFIGURED
    KoDriverServerConnectionStatus.CHECKING -> ServerConnectionStatus.CHECKING
    KoDriverServerConnectionStatus.CONNECTED -> ServerConnectionStatus.CONNECTED
    KoDriverServerConnectionStatus.DISCONNECTED -> ServerConnectionStatus.DISCONNECTED
}
