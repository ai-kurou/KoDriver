package kurou.kodriver.feature.otherkeepscreenondetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kurou.kodriver.domain.usecase.ObserveKeepScreenOnUseCase
import kurou.kodriver.domain.usecase.SaveKeepScreenOnUseCase

class OtherKeepScreenOnDetailViewModel(
    observeKeepScreenOn: ObserveKeepScreenOnUseCase,
    private val saveKeepScreenOn: SaveKeepScreenOnUseCase,
) : ViewModel() {

    private val pendingValue = MutableStateFlow<Boolean?>(null)

    val uiState = combine(observeKeepScreenOn(), pendingValue) { saved, pending ->
        OtherKeepScreenOnDetailUiState(
            keepScreenOn = saved,
            pendingKeepScreenOn = pending ?: saved,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), OtherKeepScreenOnDetailUiState())

    fun onPendingValueChanged(enabled: Boolean) {
        pendingValue.value = enabled
    }

    fun onConfirm() {
        val value = pendingValue.value ?: return
        viewModelScope.launch { saveKeepScreenOn(value) }
        pendingValue.value = null
    }

    fun onDismiss() {
        pendingValue.value = null
    }
}
