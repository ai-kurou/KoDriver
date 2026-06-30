package kurou.kodriver.feature.telemetrylogdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kurou.kodriver.domain.usecase.ObserveTelemetryLogDetailUseCase

internal class TelemetryLogDetailViewModel(
    private val observeTelemetryLogDetail: ObserveTelemetryLogDetailUseCase,
) : ViewModel() {
    private val selectedLogId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<TelemetryLogDetailUiState> = selectedLogId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(TelemetryLogDetailUiState())
            } else {
                observeTelemetryLogDetail(id).map { detail ->
                    TelemetryLogDetailUiState(
                        logId = id,
                        items = detail.toItems(),
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = TelemetryLogDetailUiState(),
        )

    fun setLogId(id: Long) {
        selectedLogId.update { id }
    }
}
