package kurou.kodriver.feature.otherconsoleipdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.ObserveGt7UdpPortUseCase
import kurou.kodriver.domain.usecase.SaveConsoleAddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7UdpPortUseCase

internal class OtherConsoleIpDetailViewModel(
    observeConsoleAddress: ObserveConsoleAddressUseCase,
    private val saveConsoleAddress: SaveConsoleAddressUseCase,
    observeGt7UdpPort: ObserveGt7UdpPortUseCase,
    private val saveGt7UdpPort: SaveGt7UdpPortUseCase,
) : ViewModel() {

    private data class MutableState(
        val userInput: String? = null,
        val saveFailed: Boolean = false,
        val isSaved: Boolean = false,
        val userSelectedPort: Int? = null,
    )

    private val savedAddress: StateFlow<String> = observeConsoleAddress()
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val savedPort: StateFlow<Int> = observeGt7UdpPort()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 33740)

    private val _mutable: MutableStateFlow<MutableState> = MutableStateFlow(MutableState())

    val uiState: StateFlow<OtherConsoleIpDetailUiState> = combine(
        savedAddress,
        savedPort,
        _mutable,
    ) { saved, port, m ->
        val current = m.userInput ?: saved
        OtherConsoleIpDetailUiState(
            inputAddress = current,
            isInputValid = current.isEmpty() || isValidIp(current),
            saveFailed = m.saveFailed,
            isSaved = m.isSaved,
            selectedPort = m.userSelectedPort ?: port,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherConsoleIpDetailUiState())

    fun onAddressChanged(address: String) {
        _mutable.update { it.copy(userInput = address) }
    }

    fun onPortSelected(port: Int) {
        _mutable.update { it.copy(userSelectedPort = port) }
    }

    fun onSave() {
        val address = _mutable.value.userInput ?: savedAddress.value
        if (!isValidIp(address)) return
        val port = _mutable.value.userSelectedPort ?: savedPort.value
        viewModelScope.launch {
            try {
                saveConsoleAddress(address)
                saveGt7UdpPort(port)
                _mutable.update { it.copy(saveFailed = false, isSaved = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _mutable.update { it.copy(saveFailed = true) }
            }
        }
    }

    fun onDismiss() {
        _mutable.update { MutableState() }
    }

    private fun isValidIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part -> part.toIntOrNull()?.let { it in 0..255 } == true }
    }
}
