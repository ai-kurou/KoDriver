package kurou.kodriver.feature.lmuwindowsreadout.brakeweardetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.model.LmuWindowsBrakeWearInvestigationData
import kurou.kodriver.domain.usecase.ObserveLmuWindowsBrakeWearInvestigationUseCase

internal class LmuWindowsReadoutBrakeWearDetailViewModel(
    observeInvestigation: ObserveLmuWindowsBrakeWearInvestigationUseCase,
) : ViewModel() {
    private val baseline = MutableStateFlow<LmuWindowsBrakeWearInvestigationData?>(null)

    val uiState: StateFlow<LmuWindowsReadoutBrakeWearDetailUiState> =
        combine(observeInvestigation(), baseline) { current, baseline ->
            LmuWindowsReadoutBrakeWearDetailUiState(current = current, baseline = baseline)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            LmuWindowsReadoutBrakeWearDetailUiState(),
        )

    /** 現在表示している値を、差分表示の基準にする。値を1つも取得できていないときは何もしない。 */
    fun onBaselineSet() {
        val state = uiState.value
        if (!state.hasCurrentValues) return
        baseline.update { state.current }
    }

    fun onBaselineCleared() {
        baseline.update { null }
    }
}
