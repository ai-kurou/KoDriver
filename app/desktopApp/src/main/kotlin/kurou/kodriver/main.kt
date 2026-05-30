package kurou.kodriver

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import kurou.kodriver.data.repository.LmuRepositoryImpl
import kurou.kodriver.domain.usecase.CheckLmuConnectionUseCase
import kurou.kodriver.domain.usecase.DisconnectLmuUseCase
import kurou.kodriver.domain.usecase.ObserveLmuUseCase
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.DashboardContent
import kurou.kodriver.presentation.LmuViewModel
import kurou.kodriver.presentation.TtsEngine

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KoDriver",
    ) {
        val repository = remember { LmuRepositoryImpl() }
        val viewModel = viewModel {
            LmuViewModel(
                observeLmu = ObserveLmuUseCase(repository),
                checkConnection = CheckLmuConnectionUseCase(repository),
                disconnect = DisconnectLmuUseCase(repository),
                ttsEngine = TtsEngine { WindowsTts.speak(it) },
            )
        }
        val uiState by viewModel.uiState.collectAsState()
        AppScreen(
            dashboardContent = { DashboardContent(uiState, viewModel::reconnect) },
        )
    }
}
