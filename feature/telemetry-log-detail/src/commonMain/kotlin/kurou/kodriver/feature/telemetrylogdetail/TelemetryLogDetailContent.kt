package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TelemetryLogDetailContent(
    id: Long,
    modifier: Modifier = Modifier,
) {
    val viewModel: TelemetryLogDetailViewModel = koinViewModel()
    LaunchedEffect(id) {
        viewModel.setLogId(id)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TelemetryLogDetailContent(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
internal fun TelemetryLogDetailContent(
    uiState: TelemetryLogDetailUiState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(uiState.items.size) {
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TelemetryLogDetailContentPreview() {
    TelemetryLogDetailContent(uiState = TelemetryLogDetailUiState())
}
