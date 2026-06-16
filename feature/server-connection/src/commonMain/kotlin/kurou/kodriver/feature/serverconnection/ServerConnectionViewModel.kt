package kurou.kodriver.feature.serverconnection

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kurou.kodriver.domain.usecase.CheckServerConnectionUseCase

data class ServerConnectionUiState(
    val isConnected: Boolean = false,
)

class ServerConnectionViewModel(
    private val checkServerConnection: CheckServerConnectionUseCase,
) : ViewModel() {

    val uiState: StateFlow<ServerConnectionUiState> = MutableStateFlow(ServerConnectionUiState())
}
