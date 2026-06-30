package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

internal const val TELEMETRY_LOG_LIST_PANE_TEST_TAG = "telemetry_log_list_pane"

@Composable
internal fun TelemetryLogListPane(
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp)
            .testTag(TELEMETRY_LOG_LIST_PANE_TEST_TAG),
    ) {
    }
}
