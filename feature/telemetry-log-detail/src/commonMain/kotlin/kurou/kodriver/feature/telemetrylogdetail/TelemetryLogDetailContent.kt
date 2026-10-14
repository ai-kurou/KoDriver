package kurou.kodriver.feature.telemetrylogdetail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.koDriverMonospaceTextStyle
import kurou.kodriver.feature.telemetrylogdetail.generated.resources.Res
import kurou.kodriver.feature.telemetrylogdetail.generated.resources.telemetry_log_detail_previous
import kurou.kodriver.feature.telemetrylogdetail.generated.resources.telemetry_log_detail_selected
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * TelemetryLogDetail のコンテンツを表示する Composable。
 */
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
        items(uiState.items) { item ->
            TelemetryLogDetailItem(item = item)
            HorizontalDivider()
        }
    }
}

@Composable
private fun TelemetryLogDetailItem(
    item: TelemetryLogDetailItemUiState,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text =
                    stringResource(
                        when (item.kind) {
                            TelemetryLogDetailItemKind.SELECTED -> Res.string.telemetry_log_detail_selected
                            TelemetryLogDetailItemKind.PREVIOUS -> Res.string.telemetry_log_detail_previous
                        },
                    ),
            )
        },
        supportingContent = {
            Text(
                text = item.telemetryJson,
                style = koDriverMonospaceTextStyle(),
                overflow = TextOverflow.Visible,
            )
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun TelemetryLogDetailContentPreview() {
    KoDriverTheme {
        TelemetryLogDetailContent(
            uiState =
                TelemetryLogDetailUiState(
                    logId = 1L,
                    items =
                        listOf(
                            TelemetryLogDetailItemUiState(
                                kind = TelemetryLogDetailItemKind.SELECTED,
                                telemetryJson = """{"speed":120,"gear":4}""",
                            ),
                            TelemetryLogDetailItemUiState(
                                kind = TelemetryLogDetailItemKind.PREVIOUS,
                                telemetryJson = """{"speed":118,"gear":4}""",
                            ),
                        ),
                ),
        )
    }
}
