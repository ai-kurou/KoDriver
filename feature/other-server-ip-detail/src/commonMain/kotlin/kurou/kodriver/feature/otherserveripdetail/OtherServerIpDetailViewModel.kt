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
) : ViewModel() {

    private val savedIp: StateFlow<String> = observeServerIp()
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _userInput: MutableStateFlow<String?> = MutableStateFlow(null)
    private val _saveFailed: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val uiState: StateFlow<OtherServerIpDetailUiState> = combine(
        savedIp,
        _userInput,
        _saveFailed,
    ) { saved, input, saveFailed ->
        val current = input ?: saved
        OtherServerIpDetailUiState(
            inputIp = current,
            isInputValid = current.isEmpty() || isValidIp(current),
            saveFailed = saveFailed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherServerIpDetailUiState())

    fun onIpChanged(ip: String) {
        _userInput.update { ip }
    }

    fun onSave() {
        val ip = _userInput.value ?: savedIp.value
        if (isValidIp(ip)) {
            viewModelScope.launch {
                try {
                    saveServerIp(ip)
                    _saveFailed.update { false }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    _saveFailed.update { true }
                }
            }
        }
    }

    fun onDismiss() {
        _userInput.update { null }
    }

    private fun isValidIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part -> part.toIntOrNull()?.let { it in 0..255 } == true }
    }
}
