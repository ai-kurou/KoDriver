package kurou.kodriver.feature.readoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuBoxScope
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.core.designsystem.simulatorIcon
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.readoutlist.generated.resources.Res
import kurou.kodriver.feature.readoutlist.generated.resources.ace_readout_timing_hint_description
import kurou.kodriver.feature.readoutlist.generated.resources.drag_handle
import kurou.kodriver.feature.readoutlist.generated.resources.priority_hint_description
import kurou.kodriver.feature.readoutlist.generated.resources.priority_hint_label
import kurou.kodriver.feature.readoutlist.generated.resources.queue_hint_description
import kurou.kodriver.feature.readoutlist.generated.resources.scroll_to_top
import kurou.kodriver.feature.readoutlist.generated.resources.select_simulator_hint
import kurou.kodriver.feature.readoutlist.generated.resources.simulator_label
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdownMenuBoxScope.SimulatorSelectorAnchor(
    selectedSimulator: Simulator?,
    expanded: Boolean,
    modifier: Modifier = Modifier,
) {
    val hint = stringResource(Res.string.select_simulator_hint)
    val selectedSimulatorName = selectedSimulator?.let { simulatorDisplayName(it.id) } ?: hint
    val shape = RoundedCornerShape(4.dp)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = shape,
                ).clip(shape)
                .semantics {
                    contentDescription = hint
                    stateDescription = selectedSimulatorName
                }.padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectedSimulator != null) {
            Image(
                painter = simulatorIcon(selectedSimulator.id),
                contentDescription = null,
                modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
            )
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.simulator_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = selectedSimulatorName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ExposedDropdownMenuDefaults.TrailingIcon(expanded)
    }
}

private fun itemIcon(itemId: ReadoutItemKey): ImageVector =
    when (itemId) {
        is ReadoutItemKey.LmuWindows -> lmuWindowsItemIcon(itemId)
        is ReadoutItemKey.Gt7Ps5 -> gt7Ps5ItemIcon(itemId)
        is ReadoutItemKey.AceWindows -> aceWindowsItemIcon(itemId)
    }

private fun lmuWindowsItemIcon(itemId: ReadoutItemKey.LmuWindows): ImageVector =
    when (itemId) {
        is ReadoutItemKey.LmuWindows.VehicleApproach -> Icons.Filled.DirectionsCar
        is ReadoutItemKey.LmuWindows.Flag -> Icons.Filled.Flag
        is ReadoutItemKey.LmuWindows.VehicleDamage -> Icons.Filled.Build
        is ReadoutItemKey.LmuWindows.TyreTemperature -> Icons.Filled.DeviceThermostat
        is ReadoutItemKey.LmuWindows.PitTiming.Root -> Icons.Filled.AccessTime
        is ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> Icons.Filled.LocalGasStation
        is ReadoutItemKey.LmuWindows.TyreWear.Root -> Icons.Filled.DonutLarge
        is ReadoutItemKey.LmuWindows.MyBestLap.Root -> Icons.Filled.Timer
    }

private fun gt7Ps5ItemIcon(itemId: ReadoutItemKey.Gt7Ps5): ImageVector =
    when (itemId) {
        is ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> Icons.Filled.Timer
        is ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> Icons.Filled.LocalGasStation
        is ReadoutItemKey.Gt7Ps5.RemainingFuel.Root -> Icons.Filled.LocalGasStation
        is ReadoutItemKey.Gt7Ps5.TyreTemperature -> Icons.Filled.DeviceThermostat
    }

private fun aceWindowsItemIcon(itemId: ReadoutItemKey.AceWindows): ImageVector =
    when (itemId) {
        is ReadoutItemKey.AceWindows.Flag -> Icons.Filled.Flag
        is ReadoutItemKey.AceWindows.RemainingFuel.Root -> Icons.Filled.LocalGasStation
        is ReadoutItemKey.AceWindows.TyreTemperature -> Icons.Filled.DeviceThermostat
    }

private fun readoutItemIndex(
    lazyListIndex: Int,
    readoutItemStartIndex: Int,
    itemCount: Int,
): Int = (lazyListIndex - readoutItemStartIndex).coerceIn(0, itemCount - 1)

internal fun readoutItemStartIndex(isAceSelected: Boolean): Int = if (isAceSelected) 3 else 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityHintRow(modifier: Modifier = Modifier) {
    var showHelpSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    if (showHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showHelpSheet = false },
            sheetState = sheetState,
        ) {
            Text(
                text = stringResource(Res.string.priority_hint_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Text(
                text = stringResource(Res.string.queue_hint_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 8.dp, bottom = 24.dp),
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.padding(bottom = 12.dp),
    ) {
        Text(
            text = stringResource(Res.string.priority_hint_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        IconButton(
            onClick = { showHelpSheet = true },
            modifier = Modifier.size(24.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = stringResource(Res.string.priority_hint_description),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AceReadoutTimingHintRow(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.ace_readout_timing_hint_description),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier.padding(bottom = 12.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadoutListPane(
    uiState: ReadoutListUiState,
    onSimulatorSelected: (Simulator) -> Unit,
    onMove: (Int, Int) -> Unit,
    onReadoutEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onQueueEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onItemClick: (ReadoutItemKey) -> Unit,
    scrollToTopRequest: Int = 0,
) {
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isAceSelected = uiState.selectedSimulator is Simulator.AceWindows
    val readoutItemStartIndex = readoutItemStartIndex(isAceSelected)
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    val reorderableState =
        rememberReorderableLazyListState(listState) { from, to ->
            onMove(
                readoutItemIndex(from.index, readoutItemStartIndex, uiState.items.size),
                readoutItemIndex(to.index, readoutItemStartIndex, uiState.items.size),
            )
        }

    ScrollToTopEffect(scrollToTopRequest = scrollToTopRequest) {
        listState.animateScrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp),
        ) {
            item(key = "simulatorSelector") {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    SimulatorSelectorAnchor(
                        selectedSimulator = uiState.selectedSimulator,
                        expanded = expanded,
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        uiState.simulators.forEach { simulator ->
                            DropdownMenuItem(
                                text = { Text(simulatorDisplayName(simulator.id)) },
                                onClick = {
                                    onSimulatorSelected(simulator)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Image(
                                        painter = simulatorIcon(simulator.id),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
                                    )
                                },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                            )
                        }
                    }
                }
            }
            if (uiState.selectedSimulator != null) {
                if (isAceSelected) {
                    item(key = "aceReadoutTimingHint") {
                        AceReadoutTimingHintRow(modifier = Modifier.padding(start = 8.dp, top = 16.dp, end = 8.dp))
                    }
                }
                item(key = "priorityHint") {
                    PriorityHintRow(
                        modifier =
                            Modifier.padding(
                                start = 8.dp,
                                top = if (isAceSelected) 0.dp else 16.dp,
                                end = 8.dp,
                            ),
                    )
                }
                itemsIndexed(uiState.items, key = { _, item -> item.value }) { index, item ->
                    ReorderableItem(reorderableState, key = item.value) {
                        val isSelected =
                            uiState.selectedSimulator.let {
                                ReadoutListItemType.fromId(it, item)
                            } == uiState.selectedItem
                        val cardContainerColor by animateColorAsState(
                            targetValue =
                                if (isSelected) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            animationSpec = tween(durationMillis = 500),
                            label = "cardContainerColor",
                        )
                        val itemName = itemDisplayName(item)
                        val readoutEnabled = uiState.readoutEnabledStates[item] ?: false
                        ReadoutListItemCard(
                            item = item,
                            index = index,
                            itemName = itemName,
                            dragHandleModifier = Modifier.draggableHandle(),
                            readoutEnabled = readoutEnabled,
                            queueEnabled = uiState.queueEnabledStates[item] ?: false,
                            containerColor = cardContainerColor,
                            onItemClick = onItemClick,
                            onQueueEnabledChanged = onQueueEnabledChanged,
                            onReadoutEnabledChanged = onReadoutEnabledChanged,
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isAtTop,
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
            ScrollToTopButton(
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                },
            )
        }
    }
}

@Composable
private fun ReadoutListItemCard(
    item: ReadoutItemKey,
    index: Int,
    itemName: String,
    dragHandleModifier: Modifier,
    readoutEnabled: Boolean,
    queueEnabled: Boolean,
    containerColor: Color,
    onItemClick: (ReadoutItemKey) -> Unit,
    onQueueEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
    onReadoutEnabledChanged: (ReadoutItemKey, Boolean) -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
                    .padding(start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier =
                    Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp)
                        .semantics { contentDescription = itemName }
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) {
                            onItemClick(item)
                        },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = stringResource(Res.string.drag_handle),
                    modifier = dragHandleModifier,
                )
                Text(
                    text = "${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 20.dp),
                )
                Icon(
                    imageVector = itemIcon(item),
                    contentDescription = null,
                )
                Text(
                    text = itemName,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (item is ReadoutItemKey.TopLevel && item.supportsQueue) {
                    ReadoutListQueueDivider()
                    ReadoutListQueueToggle(
                        item = item,
                        checked = queueEnabled,
                        enabled = readoutEnabled,
                        onCheckedChange = { onQueueEnabledChanged(item, it) },
                    )
                    ReadoutListQueueDivider()
                }
                ReadoutListReadoutSwitch(
                    item = item,
                    checked = readoutEnabled,
                    onCheckedChange = { onReadoutEnabledChanged(item, it) },
                )
            }
        }
    }
}

@Composable
private fun ReadoutListQueueToggle(
    item: ReadoutItemKey.TopLevel,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(width = 56.dp, height = 56.dp)
                .testTag("readoutListQueueTouchTarget:${item.value}")
                .clickable(
                    enabled = enabled,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    onCheckedChange(!checked)
                },
    ) {
        FilledIconToggleButton(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ReadoutListReadoutSwitch(
    item: ReadoutItemKey,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .size(width = 64.dp, height = 56.dp)
                .testTag("readoutListSwitchTouchTarget:${item.value}")
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    onCheckedChange(!checked)
                },
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun ReadoutListQueueDivider() {
    VerticalDivider(
        modifier = Modifier.height(40.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
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
private fun ScrollToTopButton(
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
        Text(stringResource(Res.string.scroll_to_top))
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadoutListPanePreview(
    @PreviewParameter(ReadoutListPanePreviewParameterProvider::class)
    uiState: ReadoutListUiState,
) {
    ReadoutListPane(
        uiState = uiState,
        onSimulatorSelected = {},
        onMove = { _, _ -> },
        onReadoutEnabledChanged = { _, _ -> },
        onQueueEnabledChanged = { _, _ -> },
        onItemClick = { _ -> },
    )
}

private class ReadoutListPanePreviewParameterProvider : PreviewParameterProvider<ReadoutListUiState> {
    override val values: Sequence<ReadoutListUiState> =
        sequenceOf(
            ReadoutListUiState(
                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                selectedSimulator = Simulator.LmuWindows,
                items =
                    listOf(
                        ReadoutItemKey.LmuWindows.VehicleApproach.Root,
                        ReadoutItemKey.LmuWindows.Flag.Root,
                        ReadoutItemKey.LmuWindows.VehicleDamage.Root,
                        ReadoutItemKey.LmuWindows.TyreTemperature.Root,
                        ReadoutItemKey.LmuWindows.MyBestLap.Root,
                    ),
            ),
            ReadoutListUiState(
                simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
                selectedSimulator = Simulator.Gt7Ps5,
                items =
                    listOf(
                        ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                        ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
                    ),
            ),
        )
}
