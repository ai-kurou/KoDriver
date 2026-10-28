package kurou.kodriver.feature.otherconsoleipdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_UDP_PORT_DEFAULT
import kurou.kodriver.domain.gt7ps5.usecase.ObserveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.gt7ps5.usecase.SaveGt7Ps5UdpPortUseCase
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase

internal class OtherConsoleIpDetailViewModel(
    observeConsoleAddress: ObserveConsoleAddressUseCase,
    private val saveConsoleAddress: SaveConsoleAddressUseCase,
    observeGt7Ps5UdpPort: ObserveGt7Ps5UdpPortUseCase,
    private val saveGt7Ps5UdpPort: SaveGt7Ps5UdpPortUseCase,
) : ViewModel() {
    private val savedAddress: StateFlow<String> =
        observeConsoleAddress()
            .map { it ?: "" }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val savedPort: StateFlow<Int> =
        observeGt7Ps5UdpPort()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GT7_PS5_UDP_PORT_DEFAULT)

    private val _mutable: MutableStateFlow<OtherConsoleIpDetailMutableState> =
        MutableStateFlow(OtherConsoleIpDetailMutableState())

    private val detailFlow =
        OtherConsoleIpDetailFlow(
            savedAddress = savedAddress,
            savedPort = savedPort,
            mutableState = _mutable,
        )

    val uiState: StateFlow<OtherConsoleIpDetailUiState> =
        detailFlow.uiState.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), detailFlow.initialUiState)

    fun onAddressChanged(address: String) {
        _mutable.update { it.copy(userInput = address) }
    }

    fun onPortSelected(port: Int) {
        _mutable.update { it.copy(userSelectedPort = port) }
    }

    fun onSave() {
        val address = _mutable.value.userInput ?: savedAddress.value
        if (!isValidConsoleIpAddress(address)) return
        val port = _mutable.value.userSelectedPort ?: savedPort.value
        viewModelScope.launch {
            try {
                saveConsoleAddress(address)
                saveGt7Ps5UdpPort(port)
                _mutable.update { it.copy(saveFailed = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                captureOtherConsoleIpDetailError(e)
                _mutable.update { it.copy(saveFailed = true) }
            }
        }
    }

    fun onDismiss() {
        _mutable.update { OtherConsoleIpDetailMutableState() }
    }
}

internal data class OtherConsoleIpDetailMutableState(
    val userInput: String? = null,
    val saveFailed: Boolean = false,
    val isSaved: Boolean = false,
    val userSelectedPort: Int? = null,
)
