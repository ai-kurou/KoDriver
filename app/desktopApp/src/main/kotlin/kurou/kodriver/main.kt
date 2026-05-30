package kurou.kodriver

import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.viewmodel.compose.viewModel
import kurou.kodriver.data.repository.LmuTelemetryRepository
import kurou.kodriver.domain.usecase.CheckTelemetryConnectionUseCase
import kurou.kodriver.domain.usecase.DisconnectTelemetryUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryUseCase
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.TelemetryViewModel
import kurou.kodriver.presentation.TtsEngine

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "KoDriver",
    ) {
        val repository = remember { LmuTelemetryRepository() }
        val viewModel = viewModel {
            TelemetryViewModel(
                observeTelemetry = ObserveTelemetryUseCase(repository),
                checkConnection = CheckTelemetryConnectionUseCase(repository),
                disconnect = DisconnectTelemetryUseCase(repository),
                ttsEngine = TtsEngine { WindowsTts.speak(it) },
            )
        }
        AppScreen(viewModel)
    }
}
