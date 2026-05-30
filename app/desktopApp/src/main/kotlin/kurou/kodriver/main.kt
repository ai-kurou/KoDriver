package kurou.kodriver

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kurou.kodriver.di.appModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.DashboardContent
import kurou.kodriver.presentation.LmuViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

fun main() = application {
    KoinApplication(application = { modules(appModule) }) {
        Window(
            onCloseRequest = ::exitApplication,
            title = "KoDriver",
        ) {
            val viewModel = koinViewModel<LmuViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AppScreen(
                dashboardContent = { DashboardContent(uiState, viewModel::reconnect) },
            )
        }
    }
}
