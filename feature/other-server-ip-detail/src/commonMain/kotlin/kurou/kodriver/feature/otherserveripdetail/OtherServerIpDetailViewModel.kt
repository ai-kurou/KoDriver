package kurou.kodriver.feature.otherserveripdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase

internal class OtherServerIpDetailViewModel(
    observeServerIp: ObserveServerIpUseCase,
    private val validateServerIpAddress: ValidateServerIpAddressUseCase,
    private val saveServerIpWithConnectivityCheck: SaveServerIpWithConnectivityCheckUseCase,
    private val windowsServerDiscovery: WindowsServerDiscovery,
) : ViewModel() {

    private data class MutableState(
        val userInput: String? = null,
        val saveFailed: Boolean = false,
        val isCheckingConnectivity: Boolean = false,
        val connectivityWarning: Boolean = false,
        val isSaved: Boolean = false,
        val isDiscoveryDialogDismissed: Boolean = false,
        val selectedDiscoveredServer: DiscoveredServer? = null,
    )

    private val savedIp: StateFlow<String> = observeServerIp()
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    // pane が画面に表示されている間だけ購読され、mDNS 検出が開始・停止する
    // （アプリ起動時ではなく WhileSubscribed により uiState の収集タイミングに連動する）
    private val discoveredServers: StateFlow<List<DiscoveredServer>> = windowsServerDiscovery.discover()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _mutable: MutableStateFlow<MutableState> = MutableStateFlow(MutableState())

    val uiState: StateFlow<OtherServerIpDetailUiState> = combine(
        savedIp,
        _mutable,
        discoveredServers,
    ) { saved, m, discovered ->
        val current = m.userInput ?: saved
        OtherServerIpDetailUiState(
            inputIp = current,
            isInputValid = current.isEmpty() || validateServerIpAddress(current),
            saveFailed = m.saveFailed,
            isCheckingConnectivity = m.isCheckingConnectivity,
            connectivityWarning = m.connectivityWarning,
            isSaved = m.isSaved,
            discoveredServers = discovered,
            isDiscoveryDialogVisible = !m.isSaved && discovered.isNotEmpty() && !m.isDiscoveryDialogDismissed,
            selectedDiscoveredServer = m.selectedDiscoveredServer ?: discovered.firstOrNull(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherServerIpDetailUiState())

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
        val ip = _mutable.value.userInput ?: savedIp.value
        viewModelScope.launch {
            _mutable.update { it.copy(isCheckingConnectivity = true, connectivityWarning = false) }
            val result = saveServerIpWithConnectivityCheck(ip)
            _mutable.update { it.copy(isCheckingConnectivity = false) }
            applySaveResult(result)
        }
    }

    fun onSaveAnyway() {
        val ip = _mutable.value.userInput ?: savedIp.value
        viewModelScope.launch {
            applySaveResult(saveServerIpWithConnectivityCheck(ip, checkConnectivity = false))
        }
    }

    fun onDismiss() {
        _mutable.update { MutableState() }
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
