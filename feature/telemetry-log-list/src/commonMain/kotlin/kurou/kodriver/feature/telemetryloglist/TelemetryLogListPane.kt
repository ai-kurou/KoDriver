package kurou.kodriver.feature.telemetryloglist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.telemetryloglist.generated.resources.Res
import kodriver.feature.telemetryloglist.generated.resources.new_telemetry_logs
import kodriver.feature.telemetryloglist.generated.resources.readout_item_blue_flag
import kodriver.feature.telemetryloglist.generated.resources.readout_item_flag
import kodriver.feature.telemetryloglist.generated.resources.readout_item_full_course_yellow
import kodriver.feature.telemetryloglist.generated.resources.readout_item_my_best_lap
import kodriver.feature.telemetryloglist.generated.resources.readout_item_overheat
import kodriver.feature.telemetryloglist.generated.resources.readout_item_red_flag
import kodriver.feature.telemetryloglist.generated.resources.readout_item_remaining_fuel_laps
import kodriver.feature.telemetryloglist.generated.resources.readout_item_remaining_virtual_energy_laps
import kodriver.feature.telemetryloglist.generated.resources.readout_item_sector_yellow_flag
import kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_low_warning
import kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_overheat_warning
import kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_temperature
import kodriver.feature.telemetryloglist.generated.resources.readout_item_tyre_wear
import kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_approach
import kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_approach_start_readout
import kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_approach_sustained
import kodriver.feature.telemetryloglist.generated.resources.readout_item_vehicle_damage
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_empty_description
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_empty_title
import kodriver.feature.telemetryloglist.generated.resources.telemetry_log_reset_item
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.generated.resources.gt7
import kurou.kodriver.core.designsystem.generated.resources.lmu
import kurou.kodriver.domain.model.ReadoutItemKey
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
    onResetClick: () -> Unit = {},
    scrollToTopRequest: Int = 0,
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
                        onClick = { onLogClick(log.id) },
                    )
                    HorizontalDivider()
                }
            }

            AnimatedVisibility(
                visible = showNewLogsButton,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(durationMillis = 300),
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(durationMillis = 200),
                ) + fadeOut(animationSpec = tween(durationMillis = 200)),
                modifier = Modifier
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
private fun TelemetryLogResetListItem(
    isResetting: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
    isSelected: Boolean,
    raceStartedAt: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(durationMillis = 500),
        label = "telemetryLogListItemContainerColor",
    )
    val headlineColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onSecondaryContainer
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(durationMillis = 500),
        label = "telemetryLogListItemHeadlineColor",
    )
    val supportingColor by animateColorAsState(
        targetValue = if (isSelected) {
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
                text = formatTelemetryLogTime(
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
                    painter = simulatorIcon(log.simulator),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = containerColor),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    )
}

@Composable
private fun simulatorIcon(simulator: Simulator): Painter = when (simulator) {
    Simulator.Gt7Ps5 -> painterResource(DesignSystemRes.drawable.gt7)
    Simulator.LmuWindows -> painterResource(DesignSystemRes.drawable.lmu)
}

@Composable
private fun flagDisplayName(flag: ReadoutItemKey.LmuWindows.Flag): String = when (flag) {
    is ReadoutItemKey.LmuWindows.Flag.Root -> stringResource(Res.string.readout_item_flag)
    is ReadoutItemKey.LmuWindows.Flag.BlueFlag -> stringResource(Res.string.readout_item_blue_flag)
    is ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag -> stringResource(Res.string.readout_item_sector_yellow_flag)
    is ReadoutItemKey.LmuWindows.Flag.FullCourseYellow -> stringResource(Res.string.readout_item_full_course_yellow)
    is ReadoutItemKey.LmuWindows.Flag.RedFlag -> stringResource(Res.string.readout_item_red_flag)
}

@Composable
private fun vehicleApproachDisplayName(vehicleApproach: ReadoutItemKey.LmuWindows.VehicleApproach): String =
    when (vehicleApproach) {
        is ReadoutItemKey.LmuWindows.VehicleApproach.Root -> stringResource(Res.string.readout_item_vehicle_approach)
        is ReadoutItemKey.LmuWindows.VehicleApproach.Sustained ->
            stringResource(Res.string.readout_item_vehicle_approach_sustained)
        is ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout ->
            stringResource(Res.string.readout_item_vehicle_approach_start_readout)
    }

@Composable
private fun tyreTemperatureDisplayName(tyreTemperature: ReadoutItemKey.LmuWindows.TyreTemperature): String =
    when (tyreTemperature) {
        is ReadoutItemKey.LmuWindows.TyreTemperature.Root -> stringResource(Res.string.readout_item_tyre_temperature)
        is ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning ->
            stringResource(Res.string.readout_item_tyre_overheat_warning)
        is ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning ->
            stringResource(Res.string.readout_item_tyre_low_warning)
    }

@Composable
private fun vehicleDamageDisplayName(vehicleDamage: ReadoutItemKey.LmuWindows.VehicleDamage): String =
    when (vehicleDamage) {
        is ReadoutItemKey.LmuWindows.VehicleDamage.Root -> stringResource(Res.string.readout_item_vehicle_damage)
        is ReadoutItemKey.LmuWindows.VehicleDamage.Overheat -> stringResource(Res.string.readout_item_overheat)
    }

@Composable
internal fun readoutItemDisplayName(readoutItemKey: ReadoutItemKey): String =
    when (readoutItemKey) {
        is ReadoutItemKey.LmuWindows.VehicleApproach -> vehicleApproachDisplayName(readoutItemKey)
        is ReadoutItemKey.LmuWindows.Flag -> flagDisplayName(readoutItemKey)
        is ReadoutItemKey.LmuWindows.VehicleDamage -> vehicleDamageDisplayName(readoutItemKey)
        is ReadoutItemKey.LmuWindows.TyreTemperature -> tyreTemperatureDisplayName(readoutItemKey)
        is ReadoutItemKey.LmuWindows.RemainingVirtualEnergyLaps.Root ->
            stringResource(Res.string.readout_item_remaining_virtual_energy_laps)
        is ReadoutItemKey.LmuWindows.TyreWear.Root -> stringResource(Res.string.readout_item_tyre_wear)
        is ReadoutItemKey.LmuWindows.MyBestLap.Root -> stringResource(Res.string.readout_item_my_best_lap)
        is ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> stringResource(Res.string.readout_item_my_best_lap)
        is ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> stringResource(Res.string.readout_item_remaining_fuel_laps)
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
