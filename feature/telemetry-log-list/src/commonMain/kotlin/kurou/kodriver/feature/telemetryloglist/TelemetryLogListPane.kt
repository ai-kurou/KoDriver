package kurou.kodriver.feature.telemetryloglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.simulatorIcon
import kurou.kodriver.domain.model.TelemetryLog
import kurou.kodriver.feature.telemetryloglist.generated.resources.Res
import kurou.kodriver.feature.telemetryloglist.generated.resources.new_telemetry_logs
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_delete_menu_item
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_empty_description
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_empty_title
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_feedback_menu_item
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_more_button
import kurou.kodriver.feature.telemetryloglist.generated.resources.telemetry_log_reset_item
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TelemetryLogListPane(
    uiState: TelemetryLogListUiState = TelemetryLogListUiState(),
    modifier: Modifier = Modifier,
    onLogClick: (Long) -> Unit = {},
    onResetClick: () -> Unit = {},
    onFeedbackClick: (Long) -> Unit = {},
    onDeleteClick: (Long) -> Unit = {},
    scrollToTopRequest: Int = 0,
    contentBottomPadding: Dp = 0.dp,
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
    val raceStartedAt =
        remember(uiState.logs) {
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

    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
        showNewLogsButton = false
    }

    LaunchedEffect(isAtTop) {
        if (isAtTop) {
            showNewLogsButton = false
        }
    }

    AnimatedVisibility(
        visible = true,
        enter =
            slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 400),
            ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        modifier = modifier.fillMaxSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp + contentBottomPadding),
            ) {
                item(key = RESET_ITEM_KEY) {
                    TelemetryLogResetListItem(
                        isResetting = uiState.isResetting,
                        onClick = onResetClick,
                    )
                    HorizontalDivider()
                }
                items(
                    items = uiState.logs,
                    key = { it.id },
                ) { log ->
                    TelemetryLogListItem(
                        log = log,
                        isSelected = log.id == uiState.selectedLogId,
                        raceStartedAt = raceStartedAt,
                        modifier = Modifier.animateItem(),
                        onClick = { onLogClick(log.id) },
                        onFeedbackClick = { onFeedbackClick(log.id) },
                        onDeleteClick = { onDeleteClick(log.id) },
                    )
                    HorizontalDivider()
                }
            }

            AnimatedVisibility(
                visible = showNewLogsButton,
                enter =
                    slideInVertically(
                        initialOffsetY = { -it },
                        animationSpec = tween(durationMillis = 300),
                    ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit =
                    slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(durationMillis = 200),
                    ) + fadeOut(animationSpec = tween(durationMillis = 200)),
                modifier =
                    Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp),
            ) {
                NewTelemetryLogsButton(
                    onClick = {
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                            showNewLogsButton = false
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ScrollToTopEffect(
    scrollToTopRequest: Int,
    scrollToTop: suspend () -> Unit,
) {
    LaunchedEffect(scrollToTopRequest) {
        if (scrollToTopRequest > 0) {
            scrollToTop()
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
        colors =
            ButtonDefaults.textButtonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
    ) {
        Text(stringResource(Res.string.new_telemetry_logs))
    }
}

@Composable
private fun TelemetryLogResetListItem(
    isResetting: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(enabled = !isResetting, onClick = onClick)
                .padding(vertical = 12.dp),
    ) {
        if (isResetting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.error,
            )
        } else {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
        }
        Text(
            text = stringResource(Res.string.telemetry_log_reset_item),
            color = MaterialTheme.colorScheme.error,
        )
    }
}

@Composable
private fun TelemetryLogEmptyState(modifier: Modifier = Modifier) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TelemetryLogListItem(
    log: TelemetryLog,
    isSelected: Boolean,
    raceStartedAt: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onFeedbackClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val containerColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        animationSpec = tween(durationMillis = 500),
        label = "telemetryLogListItemContainerColor",
    )
    val headlineColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        animationSpec = tween(durationMillis = 500),
        label = "telemetryLogListItemHeadlineColor",
    )
    val supportingColor by animateColorAsState(
        targetValue =
            if (isSelected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec = tween(durationMillis = 500),
        label = "telemetryLogListItemSupportingColor",
    )

    ListItem(
        headlineContent = {
            Text(
                text = readoutItemDisplayName(log.readoutItemKey),
                color = headlineColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        supportingContent = {
            Text(
                text =
                    formatTelemetryLogTime(
                        createdAt = log.createdAt,
                        raceElapsedMs = (log.createdAt - raceStartedAt).coerceAtLeast(0),
                    ),
                color = supportingColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        leadingContent = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(width = 40.dp, height = 64.dp),
            ) {
                Image(
                    painter = simulatorIcon(log.simulator.id),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(6.dp)),
                )
            }
        },
        trailingContent = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(Res.string.telemetry_log_more_button),
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.telemetry_log_feedback_menu_item)) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Feedback, contentDescription = null) },
                        onClick = {
                            menuExpanded = false
                            onFeedbackClick()
                        },
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(Res.string.telemetry_log_delete_menu_item),
                                color = MaterialTheme.colorScheme.error,
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        },
                    )
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { menuExpanded = true },
                ),
    )
}

internal fun formatTelemetryLogTime(
    createdAt: Long,
    raceElapsedMs: Long,
): String = "${formatTimeOfDay(createdAt)} / レース +${formatDuration(raceElapsedMs)}"

private fun formatTimeOfDay(milliseconds: Long): String {
    val millisInDay = (milliseconds + JST_OFFSET_MILLIS).floorMod(MILLISECONDS_PER_DAY)
    return formatDuration(millisInDay)
}

private fun formatDuration(milliseconds: Long): String {
    val hours = milliseconds / MILLISECONDS_PER_HOUR
    val minutes = milliseconds % MILLISECONDS_PER_HOUR / MILLISECONDS_PER_MINUTE
    val seconds = milliseconds % MILLISECONDS_PER_MINUTE / MILLISECONDS_PER_SECOND
    val millis = milliseconds % MILLISECONDS_PER_SECOND

    return "${hours.pad(2)}:${minutes.pad(2)}:${seconds.pad(2)}.${millis.pad(3)}"
}

private fun Long.floorMod(other: Long): Long = ((this % other) + other) % other

private fun Long.pad(length: Int): String = toString().padStart(length, '0')

private const val FIRST_VISIBLE_ITEM_INDEX_FOR_AUTO_SCROLL = 1
private const val RESET_ITEM_KEY = "telemetry_log_reset_item"
private const val MILLISECONDS_PER_SECOND = 1_000L
private const val MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND
private const val MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE
private const val MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR
private const val JST_OFFSET_MILLIS = 9 * MILLISECONDS_PER_HOUR

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
