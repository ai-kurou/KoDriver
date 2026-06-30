package kurou.kodriver.feature.telemetryloglist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.TelemetryLog

@Composable
internal fun TelemetryLogListPane(
    uiState: TelemetryLogListUiState = TelemetryLogListUiState(),
    modifier: Modifier = Modifier,
) {
    if (uiState.logs.isEmpty()) {
        TelemetryLogEmptyState(
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        items(
            items = uiState.logs,
            key = { it.id },
        ) { log ->
            TelemetryLogListItem(log = log)
            HorizontalDivider()
        }
    }
}

@Composable
private fun TelemetryLogEmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "ログはまだありません",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "テレメトリを受信すると、ここに新しい順で表示されます。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TelemetryLogListItem(
    log: TelemetryLog,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text = log.readoutItemKey,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text = log.telemetryJson,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        overlineContent = {
            Text(
                text = "${log.simulatorId} / ${log.createdAt}",
                style = MaterialTheme.typography.labelMedium,
            )
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun TelemetryLogListPanePreview() {
    TelemetryLogListPane(
        uiState = previewTelemetryLogListUiState,
    )
}

@Preview(showBackground = true)
@Composable
private fun TelemetryLogListPaneEmptyPreview() {
    TelemetryLogListPane()
}
