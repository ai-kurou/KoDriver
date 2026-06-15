package kurou.kodriver.feature.otherserveripdetail

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
import kurou.kodriver.domain.usecase.ObserveServerIpUseCase
import kurou.kodriver.domain.usecase.SaveServerIpUseCase

internal class OtherServerIpDetailViewModel(
    observeServerIp: ObserveServerIpUseCase,
    private val saveServerIp: SaveServerIpUseCase,
    private val connectivityChecker: ServerConnectivityChecker,
) : ViewModel() {

    private data class MutableState(
        val userInput: String? = null,
        val saveFailed: Boolean = false,
        val isCheckingConnectivity: Boolean = false,
        val connectivityWarning: Boolean = false,
        val isSaved: Boolean = false,
    )

    private val savedIp: StateFlow<String> = observeServerIp()
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _mutable: MutableStateFlow<MutableState> = MutableStateFlow(MutableState())

    val uiState: StateFlow<OtherServerIpDetailUiState> = combine(
        savedIp,
        _mutable,
    ) { saved, m ->
        val current = m.userInput ?: saved
        OtherServerIpDetailUiState(
            inputIp = current,
            isInputValid = current.isEmpty() || isValidIp(current),
            saveFailed = m.saveFailed,
            isCheckingConnectivity = m.isCheckingConnectivity,
            connectivityWarning = m.connectivityWarning,
            isSaved = m.isSaved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherServerIpDetailUiState())

    fun onIpChanged(ip: String) {
        _mutable.update { it.copy(userInput = ip, connectivityWarning = false) }
    }

    fun onSave() {
        if (_mutable.value.isCheckingConnectivity) return
        val ip = _mutable.value.userInput ?: savedIp.value
        if (!isValidIp(ip)) return
        viewModelScope.launch {
            _mutable.update { it.copy(isCheckingConnectivity = true, connectivityWarning = false) }
            val reachable = connectivityChecker.isReachable(ip)
            _mutable.update { it.copy(isCheckingConnectivity = false) }
            if (!reachable) {
                _mutable.update { it.copy(connectivityWarning = true) }
                return@launch
            }
            doSave(ip)
        }
    }

    fun onSaveAnyway() {
        val ip = _mutable.value.userInput ?: savedIp.value
        if (!isValidIp(ip)) return
        viewModelScope.launch { doSave(ip) }
    }

    fun onDismiss() {
        _mutable.update { MutableState() }
    }

    private suspend fun doSave(ip: String) {
        try {
            saveServerIp(ip)
            _mutable.update { it.copy(saveFailed = false, isSaved = true) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _mutable.update { it.copy(saveFailed = true) }
        }
    }

    private fun isValidIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part -> part.toIntOrNull()?.let { it in 0..255 } == true }
    }
}
