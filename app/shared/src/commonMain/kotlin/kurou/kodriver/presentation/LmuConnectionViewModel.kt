package kurou.kodriver.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase

internal data class LmuConnectionUiState(
    val isConnected: Boolean = false,
)

internal class LmuConnectionViewModel(
    private val checkLmuConnection: CheckLmuConnectionUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuConnectionUiState> = flow {
        while (true) {
            val isConnected = runCatching { checkLmuConnection() }.getOrDefault(false)
            emit(LmuConnectionUiState(isConnected = isConnected))
            delay(CONNECTION_CHECK_INTERVAL_MS)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = LmuConnectionUiState(),
    )

    private companion object {
        const val CONNECTION_CHECK_INTERVAL_MS = 1_000L
    }
}
