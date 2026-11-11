package kurou.kodriver.feature.otherserveripdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.launchCapturingErrors
import kurou.kodriver.domain.usecase.CheckAccessLocalNetworkPermissionGrantedUseCase
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

@OptIn(ExperimentalCoroutinesApi::class)
internal class OtherServerIpDetailViewModel(
    observeServerIp: ObserveServerIpUseCase,
    validateServerIpAddress: ValidateServerIpAddressUseCase,
    private val saveServerIpWithConnectivityCheck: SaveServerIpWithConnectivityCheckUseCase,
    private val windowsServerDiscovery: WindowsServerDiscovery,
    private val checkAccessLocalNetworkPermissionGranted: CheckAccessLocalNetworkPermissionGrantedUseCase,
) : ViewModel() {
    private val savedIp: StateFlow<String?> =
        observeServerIp()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val accessLocalNetworkPermissionGranted = MutableStateFlow(checkAccessLocalNetworkPermissionGranted())

    // pane が画面に表示されている間だけ購読され、mDNS 検出が開始・停止する
    // （アプリ起動時ではなく WhileSubscribed により uiState の収集タイミングに連動する）。
    // ローカルネットワークの権限がない間は検出を始めず、許可されたときに検出を始める
    private val discoveredServers: StateFlow<List<DiscoveredServer>> =
        accessLocalNetworkPermissionGranted
            .flatMapLatest { granted -> if (granted) windowsServerDiscovery.discover() else flowOf(emptyList()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mutable: MutableStateFlow<OtherServerIpDetailMutableState> =
        MutableStateFlow(OtherServerIpDetailMutableState())

    private val detailFlow =
        OtherServerIpDetailFlow(
            savedIp = savedIp,
            mutableState = _mutable,
            discoveredServers = discoveredServers,
            accessLocalNetworkPermissionGranted = accessLocalNetworkPermissionGranted,
            validateServerIpAddress = validateServerIpAddress,
        )

    val uiState: StateFlow<OtherServerIpDetailUiState> =
        detailFlow.uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), detailFlow.initialUiState)

    /** 画面が前面に戻るたびに権限を確認し直す。権限の要求ダイアログや設定画面から戻った直後に検出を始めるため。 */
    fun onResumed() {
        accessLocalNetworkPermissionGranted.value = checkAccessLocalNetworkPermissionGranted()
    }

    fun onIpChanged(ip: String) {
        _mutable.update { it.copy(userInput = ip, connectivityWarning = false) }
    }

    fun onDiscoveredServerSelected(server: DiscoveredServer) {
        _mutable.update { it.copy(selectedDiscoveredServer = server) }
    }

    fun onDiscoveryDialogConfirm() {
        val server = _mutable.value.selectedDiscoveredServer ?: discoveredServers.value.firstOrNull() ?: return
        _mutable.update {
            it.copy(userInput = server.ipAddress, isDiscoveryDialogDismissed = true, connectivityWarning = false)
        }
    }

    fun onDiscoveryDialogDismiss() {
        _mutable.update { it.copy(isDiscoveryDialogDismissed = true) }
    }

    fun onShowDiscoveredServers() {
        _mutable.update { it.copy(isDiscoveryDialogDismissed = false) }
    }

    fun onSave() {
        if (_mutable.value.isCheckingConnectivity) return
        val ip = _mutable.value.userInput ?: savedIp.value ?: ""
        viewModelScope.launchCapturingErrors {
            _mutable.update { it.copy(isCheckingConnectivity = true, connectivityWarning = false) }
            try {
                applySaveResult(saveServerIpWithConnectivityCheck(ip))
            } finally {
                _mutable.update { it.copy(isCheckingConnectivity = false) }
            }
        }
    }

    fun onSaveAnyway() {
        val ip = _mutable.value.userInput ?: savedIp.value ?: ""
        viewModelScope.launchCapturingErrors {
            applySaveResult(saveServerIpWithConnectivityCheck(ip, checkConnectivity = false))
        }
    }

    fun onDismiss() {
        _mutable.update { OtherServerIpDetailMutableState() }
    }

    private fun applySaveResult(result: SaveServerIpResult) {
        when (result) {
            SaveServerIpResult.Saved -> _mutable.update { it.copy(saveFailed = false, isSaved = true) }
            SaveServerIpResult.Unreachable -> _mutable.update { it.copy(connectivityWarning = true) }
            SaveServerIpResult.SaveFailed -> _mutable.update { it.copy(saveFailed = true) }
            SaveServerIpResult.InvalidIp -> Unit
        }
    }
}

internal data class OtherServerIpDetailMutableState(
    val userInput: String? = null,
    val saveFailed: Boolean = false,
    val isCheckingConnectivity: Boolean = false,
    val connectivityWarning: Boolean = false,
    val isSaved: Boolean = false,
    val isDiscoveryDialogDismissed: Boolean = false,
    val selectedDiscoveredServer: DiscoveredServer? = null,
)
