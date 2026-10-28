package kurou.kodriver.feature.telemetrylogdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase

internal class TelemetryLogDetailViewModel(
    observeTelemetryLogDetail: ObserveTelemetryLogDetailUseCase,
) : ViewModel() {
    private val selectedLogId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<TelemetryLogDetailUiState> =
        TelemetryLogDetailFlow(
            selectedLogId = selectedLogId,
            observeTelemetryLogDetail = observeTelemetryLogDetail,
        ).uiState.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TelemetryLogDetailUiState(),
        )

    fun setLogId(id: Long) {
        selectedLogId.update { id }
    }
}
