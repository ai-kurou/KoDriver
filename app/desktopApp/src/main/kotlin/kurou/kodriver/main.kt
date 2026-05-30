package kurou.kodriver

import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import java.awt.Dimension
import kurou.kodriver.di.appModule
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.ReadoutContent
import kurou.kodriver.presentation.LmuViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

fun main() = application {
    KoinApplication(application = { modules(appModule) }) {
        val windowState = rememberWindowState(size = DpSize(1200.dp, 700.dp))
        Window(
            onCloseRequest = ::exitApplication,
            title = "KoDriver",
            state = windowState,
        ) {
            SideEffect { window.minimumSize = Dimension(900, 600) }
            val viewModel = koinViewModel<LmuViewModel>()
            val uiState by viewModel.uiState.collectAsState()
            AppScreen(
                readoutContent = { ReadoutContent(uiState, viewModel::reconnect) },
            )
        }
    }
}
