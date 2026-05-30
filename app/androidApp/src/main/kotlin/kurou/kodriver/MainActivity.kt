package kurou.kodriver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import kurou.kodriver.domain.usecase.CheckTelemetryConnectionUseCase
import kurou.kodriver.domain.usecase.DisconnectTelemetryUseCase
import kurou.kodriver.domain.usecase.ObserveTelemetryUseCase
import kurou.kodriver.presentation.AppScreen
import kurou.kodriver.presentation.TelemetryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val repository = StubTelemetryRepository()

        setContent {
            val viewModel = viewModel {
                TelemetryViewModel(
                    observeTelemetry = ObserveTelemetryUseCase(repository),
                    checkConnection = CheckTelemetryConnectionUseCase(repository),
                    disconnect = DisconnectTelemetryUseCase(repository),
                )
            }
            AppScreen(viewModel)
        }
    }
}
