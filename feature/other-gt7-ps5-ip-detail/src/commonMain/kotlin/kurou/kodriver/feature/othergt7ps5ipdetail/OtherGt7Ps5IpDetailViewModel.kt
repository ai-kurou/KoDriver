package kurou.kodriver.feature.othergt7ps5ipdetail

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
import kurou.kodriver.domain.usecase.ObserveGt7Ps5AddressUseCase
import kurou.kodriver.domain.usecase.SaveGt7Ps5AddressUseCase

internal class OtherGt7Ps5IpDetailViewModel(
    observeGt7Ps5Address: ObserveGt7Ps5AddressUseCase,
    private val saveGt7Ps5Address: SaveGt7Ps5AddressUseCase,
) : ViewModel() {

    private data class MutableState(
        val userInput: String? = null,
        val saveFailed: Boolean = false,
        val isSaved: Boolean = false,
    )

    private val savedAddress: StateFlow<String> = observeGt7Ps5Address()
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _mutable: MutableStateFlow<MutableState> = MutableStateFlow(MutableState())

    val uiState: StateFlow<OtherGt7Ps5IpDetailUiState> = combine(
        savedAddress,
        _mutable,
    ) { saved, m ->
        val current = m.userInput ?: saved
        OtherGt7Ps5IpDetailUiState(
            inputAddress = current,
            isInputValid = current.isEmpty() || isValidIp(current),
            saveFailed = m.saveFailed,
            isSaved = m.isSaved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherGt7Ps5IpDetailUiState())

    fun onAddressChanged(address: String) {
        _mutable.update { it.copy(userInput = address) }
    }

    fun onSave() {
        val address = _mutable.value.userInput ?: savedAddress.value
        if (!isValidIp(address)) return
        viewModelScope.launch {
            try {
                saveGt7Ps5Address(address)
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
