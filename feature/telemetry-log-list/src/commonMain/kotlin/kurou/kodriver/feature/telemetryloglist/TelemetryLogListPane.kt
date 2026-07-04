package kurou.kodriver.feature.telemetryloglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.telemetryloglist.generated.resources.Res
import kodriver.feature.telemetryloglist.generated.resources.new_telemetry_logs
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_empty_description
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_empty_title
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.generated.resources.gt7
import kurou.kodriver.core.designsystem.generated.resources.lmu
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.TelemetryLog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kurou.kodriver.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
internal fun TelemetryLogListPane(
    uiState: TelemetryLogListUiState = TelemetryLogListUiState(),
    modifier: Modifier = Modifier,
    onLogClick: (Long) -> Unit = {},
) {
    if (uiState.logs.isEmpty()) {
        TelemetryLogEmptyState(
            modifier = modifier.fillMaxSize(),
        )
        return
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val firstLogId = uiState.logs.first().id
    val raceStartedAt = remember(uiState.logs) {
        uiState.logs.minOf { it.createdAt }
    }
    var previousFirstLogId by remember { mutableLongStateOf(firstLogId) }
    var showNewLogsButton by remember { mutableStateOf(false) }
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(firstLogId) {
        if (previousFirstLogId != firstLogId) {
            if (listState.firstVisibleItemIndex <= FIRST_VISIBLE_ITEM_INDEX_FOR_AUTO_SCROLL) {
                listState.animateScrollToItem(0)
            } else {
                showNewLogsButton = true
            }
            previousFirstLogId = firstLogId
        }
    }

    LaunchedEffect(isAtTop) {
        if (isAtTop) {
            showNewLogsButton = false
        }
    }

    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 400),
        ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
            ) {
                items(
                    items = uiState.logs,
                    key = { it.id },
                ) { log ->
                    TelemetryLogListItem(
                        log = log,
                        raceStartedAt = raceStartedAt,
                        onClick = { onLogClick(log.id) },
                    )
                    HorizontalDivider()
                }
            }

            if (showNewLogsButton) {
                NewTelemetryLogsButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                            showNewLogsButton = false
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun NewTelemetryLogsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        Text(stringResource(Res.string.new_telemetry_logs))
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
                text = stringResource(Res.string.telemetry_log_empty_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.telemetry_log_empty_description),
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
    raceStartedAt: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
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
                text = formatTelemetryLogTime(
                    createdAt = log.createdAt,
                    raceElapsedMs = (log.createdAt - raceStartedAt).coerceAtLeast(0),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        overlineContent = {
            Text(
                text = "${log.simulatorId} / ${log.createdAt}",
                style = MaterialTheme.typography.labelMedium,
            )
        },
        leadingContent = simulatorIcon(log.simulatorId)?.let { painter ->
            {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Composable
private fun simulatorIcon(simulatorId: String): Painter? = when (simulatorId) {
    Simulator.Gt7Ps5.id -> painterResource(DesignSystemRes.drawable.gt7)
    Simulator.LmuWindows.id -> painterResource(DesignSystemRes.drawable.lmu)
    else -> null
}

internal fun formatTelemetryLogTime(
    createdAt: Long,
    raceElapsedMs: Long,
): String = "${formatTimeOfDay(createdAt)} / レース +${formatDuration(raceElapsedMs)}"

private fun formatTimeOfDay(milliseconds: Long): String {
    val millisInDay = milliseconds.floorMod(MILLISECONDS_PER_DAY)
    return formatDuration(millisInDay)
}

private fun formatDuration(milliseconds: Long): String {
    val hours = milliseconds / MILLISECONDS_PER_HOUR
    val minutes = milliseconds % MILLISECONDS_PER_HOUR / MILLISECONDS_PER_MINUTE
    val seconds = milliseconds % MILLISECONDS_PER_MINUTE / MILLISECONDS_PER_SECOND
    val millis = milliseconds % MILLISECONDS_PER_SECOND

    return "${hours.pad2()}:${minutes.pad2()}:${seconds.pad2()}.${millis.pad3()}"
}

private fun Long.floorMod(other: Long): Long = ((this % other) + other) % other

private fun Long.pad2(): String = toString().padStart(2, '0')

private fun Long.pad3(): String = toString().padStart(3, '0')

private const val FIRST_VISIBLE_ITEM_INDEX_FOR_AUTO_SCROLL = 1
private const val MILLISECONDS_PER_SECOND = 1_000L
private const val MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND
private const val MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE
private const val MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR

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
