package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TelemetryLogContent(
    modifier: Modifier = Modifier,
) {
    koinViewModel<TelemetryLogListViewModel>()
    TelemetryLogContentBody(modifier = modifier)
}

@Composable
private fun TelemetryLogContentBody(
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
    }
}

@Preview(showBackground = true)
@Composable
private fun TelemetryLogContentPreview() {
    TelemetryLogContentBody()
}
