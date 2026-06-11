package kurou.kodriver.feature.lmuconnection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase

data class LmuConnectionUiState(
    val isConnected: Boolean = false,
    val isConnectionChecked: Boolean = false,
)

class LmuConnectionViewModel(
    private val checkLmuConnection: CheckLmuConnectionUseCase,
) : ViewModel() {

    val uiState: StateFlow<LmuConnectionUiState> = flow {
        while (true) {
            val isConnected = try {
                checkLmuConnection()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                false
            }
            emit(
                LmuConnectionUiState(
                    isConnected = isConnected,
                    isConnectionChecked = true,
                ),
            )
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
