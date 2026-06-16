package kurou.kodriver.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.feature.lmuconnection.LmuConnectionViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
actual fun rememberConnectionBannerUiState(): ConnectionBannerUiState {
    val viewModel: LmuConnectionViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    return ConnectionBannerUiState(
        isConnected = uiState.isConnected,
        isConnectionChecked = uiState.isConnectionChecked,
    )
}
