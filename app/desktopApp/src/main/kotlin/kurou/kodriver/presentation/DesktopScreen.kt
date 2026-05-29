package kurou.kodriver.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun DesktopScreen(viewModel: TelemetryViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center,
        ) {
            when (val state = uiState) {
                is TelemetryUiState.Connecting -> Text("接続中...", fontSize = 24.sp)
                is TelemetryUiState.Error -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("エラー: ${state.message}", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::reconnect) { Text("再接続") }
                }
                is TelemetryUiState.Connected -> Text(
                    text = "${state.data.vehicle.speedKmh.toInt()} km/h",
                    fontSize = 96.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}
