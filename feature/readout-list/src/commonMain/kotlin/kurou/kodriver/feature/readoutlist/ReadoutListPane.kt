package kurou.kodriver.feature.readoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.drag_handle
import kodriver.feature.readoutlist.generated.resources.item_blue_flag
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_full_course_yellow
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_overheat
import kodriver.feature.readoutlist.generated.resources.item_pit_timing
import kodriver.feature.readoutlist.generated.resources.item_red_flag
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_remaining_virtual_energy
import kodriver.feature.readoutlist.generated.resources.item_sector_yellow_flag
import kodriver.feature.readoutlist.generated.resources.item_tyre_low_warning
import kodriver.feature.readoutlist.generated.resources.item_tyre_overheat_warning
import kodriver.feature.readoutlist.generated.resources.item_tyre_temperature
import kodriver.feature.readoutlist.generated.resources.item_tyre_wear
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach_start_readout
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach_sustained
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kodriver.feature.readoutlist.generated.resources.priority_hint_description
import kodriver.feature.readoutlist.generated.resources.priority_hint_label
import kodriver.feature.readoutlist.generated.resources.queue_hint_description
import kodriver.feature.readoutlist.generated.resources.scroll_to_top
import kodriver.feature.readoutlist.generated.resources.select_simulator_hint
import kodriver.feature.readoutlist.generated.resources.simulator_label
import kodriver.feature.readoutlist.generated.resources.simulator_name_ace_windows
import kodriver.feature.readoutlist.generated.resources.simulator_name_gt7_ps5
import kodriver.feature.readoutlist.generated.resources.simulator_name_lmu_windows
import kotlinx.coroutines.launch
import kurou.kodriver.core.designsystem.ListPaneCard
import kurou.kodriver.core.designsystem.generated.resources.ace
import kurou.kodriver.core.designsystem.generated.resources.gt7
import kurou.kodriver.core.designsystem.generated.resources.lmu
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import kurou.kodriver.core.designsystem.generated.resources.Res as DesignSystemRes

@Composable
private fun simulatorDisplayName(simulator: Simulator): String = when (simulator) {
    is Simulator.LmuWindows -> stringResource(Res.string.simulator_name_lmu_windows)
    is Simulator.Gt7Ps5 -> stringResource(Res.string.simulator_name_gt7_ps5)
    is Simulator.AceWindows -> stringResource(Res.string.simulator_name_ace_windows)
}

@Composable
private fun simulatorIcon(simulator: Simulator) = when (simulator) {
    is Simulator.Gt7Ps5 -> painterResource(DesignSystemRes.drawable.gt7)
    is Simulator.LmuWindows -> painterResource(DesignSystemRes.drawable.lmu)
    is Simulator.AceWindows -> painterResource(DesignSystemRes.drawable.ace)
}

@Composable
private fun flagItemDisplayName(flag: ReadoutItemKey.LmuWindows.Flag): String = when (flag) {
    is ReadoutItemKey.LmuWindows.Flag.Root -> stringResource(Res.string.item_flag)
    is ReadoutItemKey.LmuWindows.Flag.BlueFlag -> stringResource(Res.string.item_blue_flag)
    is ReadoutItemKey.LmuWindows.Flag.SectorYellowFlag -> stringResource(Res.string.item_sector_yellow_flag)
    is ReadoutItemKey.LmuWindows.Flag.FullCourseYellow -> stringResource(Res.string.item_full_course_yellow)
    is ReadoutItemKey.LmuWindows.Flag.RedFlag -> stringResource(Res.string.item_red_flag)
}

@Composable
private fun vehicleApproachItemDisplayName(vehicleApproach: ReadoutItemKey.LmuWindows.VehicleApproach): String =
    when (vehicleApproach) {
        is ReadoutItemKey.LmuWindows.VehicleApproach.Root -> stringResource(Res.string.item_vehicle_approach)
        is ReadoutItemKey.LmuWindows.VehicleApproach.Sustained ->
            stringResource(Res.string.item_vehicle_approach_sustained)
        is ReadoutItemKey.LmuWindows.VehicleApproach.StartReadout ->
            stringResource(Res.string.item_vehicle_approach_start_readout)
    }

@Composable
private fun tyreTemperatureItemDisplayName(tyreTemperature: ReadoutItemKey.LmuWindows.TyreTemperature): String =
    when (tyreTemperature) {
        is ReadoutItemKey.LmuWindows.TyreTemperature.Root -> stringResource(Res.string.item_tyre_temperature)
        is ReadoutItemKey.LmuWindows.TyreTemperature.OverheatWarning ->
            stringResource(Res.string.item_tyre_overheat_warning)
        is ReadoutItemKey.LmuWindows.TyreTemperature.LowWarning ->
            stringResource(Res.string.item_tyre_low_warning)
    }

@Composable
private fun vehicleDamageItemDisplayName(vehicleDamage: ReadoutItemKey.LmuWindows.VehicleDamage): String =
    when (vehicleDamage) {
        is ReadoutItemKey.LmuWindows.VehicleDamage.Root -> stringResource(Res.string.item_vehicle_damage)
        is ReadoutItemKey.LmuWindows.VehicleDamage.Overheat -> stringResource(Res.string.item_overheat)
    }

@Composable
private fun itemDisplayName(itemId: ReadoutItemKey): String = when (itemId) {
    is ReadoutItemKey.LmuWindows.VehicleApproach -> vehicleApproachItemDisplayName(itemId)
    is ReadoutItemKey.LmuWindows.Flag -> flagItemDisplayName(itemId)
    is ReadoutItemKey.LmuWindows.VehicleDamage -> vehicleDamageItemDisplayName(itemId)
    is ReadoutItemKey.LmuWindows.TyreTemperature -> tyreTemperatureItemDisplayName(itemId)
    is ReadoutItemKey.LmuWindows.PitTiming.Root -> stringResource(Res.string.item_pit_timing)
    is ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root ->
        stringResource(Res.string.item_remaining_virtual_energy)
    is ReadoutItemKey.LmuWindows.TyreWear.Root -> stringResource(Res.string.item_tyre_wear)
    is ReadoutItemKey.LmuWindows.MyBestLap.Root -> stringResource(Res.string.item_my_best_lap)
    is ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> stringResource(Res.string.item_my_best_lap)
    is ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> stringResource(Res.string.item_remaining_fuel_laps)
    is ReadoutItemKey.AceWindows.RemainingFuel.Root -> stringResource(Res.string.item_remaining_fuel)
}

private fun itemIcon(itemId: ReadoutItemKey): ImageVector = when (itemId) {
    is ReadoutItemKey.LmuWindows.VehicleApproach -> Icons.Filled.DirectionsCar
    is ReadoutItemKey.LmuWindows.Flag -> Icons.Filled.Flag
    is ReadoutItemKey.LmuWindows.VehicleDamage -> Icons.Filled.Build
    is ReadoutItemKey.LmuWindows.TyreTemperature -> Icons.Filled.DeviceThermostat
    is ReadoutItemKey.LmuWindows.PitTiming.Root -> Icons.Filled.AccessTime
    is ReadoutItemKey.LmuWindows.RemainingVirtualEnergy.Root -> Icons.Filled.LocalGasStation
    is ReadoutItemKey.LmuWindows.TyreWear.Root -> Icons.Filled.DonutLarge
    is ReadoutItemKey.LmuWindows.MyBestLap.Root -> Icons.Filled.Timer
    is ReadoutItemKey.Gt7Ps5.MyBestLap.Root -> Icons.Filled.Timer
    is ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root -> Icons.Filled.LocalGasStation
    is ReadoutItemKey.AceWindows.RemainingFuel.Root -> Icons.Filled.LocalGasStation
}

private fun readoutItemIndex(
    lazyListIndex: Int,
    readoutItemStartIndex: Int,
    itemCount: Int,
): Int = (lazyListIndex - readoutItemStartIndex).coerceIn(0, itemCount - 1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityHintRow(
    modifier: Modifier = Modifier,
) {
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
    val readoutItemStartIndex = 2
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
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
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
        ) {
            item(key = "simulatorSelector") {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier.padding(horizontal = 8.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.selectedSimulator
                            ?.let { simulatorDisplayName(it) }
                            ?: stringResource(Res.string.select_simulator_hint),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.simulator_label)) },
                        leadingIcon = if (uiState.selectedSimulator != null) {
                            {
                                Image(
                                    painter = simulatorIcon(uiState.selectedSimulator),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
                                )
                            }
                        } else {
                            null
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = run {
                            val hint = stringResource(Res.string.select_simulator_hint)
                            Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .semantics { contentDescription = hint }
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        uiState.simulators.forEach { simulator ->
                            DropdownMenuItem(
                                text = { Text(simulatorDisplayName(simulator)) },
                                onClick = {
                                    onSimulatorSelected(simulator)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Image(
                                        painter = simulatorIcon(simulator),
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
                item(key = "priorityHint") {
                    PriorityHintRow(modifier = Modifier.padding(start = 8.dp, top = 16.dp, end = 8.dp))
                }
                itemsIndexed(uiState.items, key = { _, it -> it.value }) { index, item ->
                    ReorderableItem(reorderableState, key = item.value) {
                        val isSelected = uiState.selectedSimulator.let {
                            ReadoutListItemType.fromId(it, item)
                        } == uiState.selectedItem
                        val cardContainerColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                            animationSpec = tween(durationMillis = 500),
                            label = "cardContainerColor",
                        )
                        val itemName = itemDisplayName(item)
                        val readoutEnabled = uiState.readoutEnabledStates[item] ?: false
                        ListPaneCard(
                            onClick = { onItemClick(item) },
                            // クリック可能なのはこの Card 自身であり、内部の headlineContent の Text 自体は
                            // クリックアクションを持たない。Compose UI Test で座標タップ(performClick)ではなく
                            // OnClick セマンティクスアクションを直接実行してクリックすると、
                            // Text ノードを対象にした場合は「ノードが OnClick を持たない」エラーになるため、
                            // Card 自身を一意に特定できるよう contentDescription を付与する。
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .semantics { contentDescription = itemName },
                            containerColor = cardContainerColor,
                        ) {
                            ListItem(
                                modifier = Modifier.clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    onItemClick(item)
                                },
                                headlineContent = { Text(itemName) },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                leadingContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DragIndicator,
                                            contentDescription = stringResource(Res.string.drag_handle),
                                            modifier = Modifier.draggableHandle(),
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
                                    }
                                },
                                trailingContent = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        if (item is ReadoutItemKey.TopLevel && item.supportsQueue) {
                                            ReadoutListQueueDivider()
                                            ReadoutListQueueToggle(
                                                item = item,
                                                checked = uiState.queueEnabledStates[item] ?: false,
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
                                },
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isAtTop,
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
private fun ReadoutListQueueToggle(
    item: ReadoutItemKey.TopLevel,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 56.dp, height = 56.dp)
            .testTag("readoutListQueueTouchTarget:${item.value}")
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                if (enabled) {
                    onCheckedChange(!checked)
                }
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
        modifier = Modifier
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
        colors = ButtonDefaults.textButtonColors(
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
    override val values: Sequence<ReadoutListUiState> = sequenceOf(
        ReadoutListUiState(
            simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
            selectedSimulator = Simulator.LmuWindows,
            items = listOf(
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
            items = listOf(
                ReadoutItemKey.Gt7Ps5.RemainingFuelLaps.Root,
                ReadoutItemKey.Gt7Ps5.MyBestLap.Root,
            ),
        ),
    )
}
