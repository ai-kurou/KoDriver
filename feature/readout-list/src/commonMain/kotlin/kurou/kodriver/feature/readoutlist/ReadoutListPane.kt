package kurou.kodriver.feature.readoutlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.drag_handle
import kodriver.feature.readoutlist.generated.resources.gt7
import kodriver.feature.readoutlist.generated.resources.item_blue_flag
import kodriver.feature.readoutlist.generated.resources.item_flag
import kodriver.feature.readoutlist.generated.resources.item_full_course_yellow
import kodriver.feature.readoutlist.generated.resources.item_my_best_lap
import kodriver.feature.readoutlist.generated.resources.item_overheat
import kodriver.feature.readoutlist.generated.resources.item_red_flag
import kodriver.feature.readoutlist.generated.resources.item_remaining_fuel_laps
import kodriver.feature.readoutlist.generated.resources.item_sector_yellow_flag
import kodriver.feature.readoutlist.generated.resources.item_vehicle_approach
import kodriver.feature.readoutlist.generated.resources.item_vehicle_damage
import kodriver.feature.readoutlist.generated.resources.lmu
import kodriver.feature.readoutlist.generated.resources.priority_hint_description
import kodriver.feature.readoutlist.generated.resources.priority_hint_label
import kodriver.feature.readoutlist.generated.resources.select_simulator_hint
import kodriver.feature.readoutlist.generated.resources.simulator_label
import kodriver.feature.readoutlist.generated.resources.simulator_name_gt7_ps5
import kodriver.feature.readoutlist.generated.resources.simulator_name_lmu
import kurou.kodriver.domain.model.ReadoutItemKey
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
private fun simulatorDisplayName(simulator: Simulator): String = when (simulator) {
    is Simulator.LmuWindows -> stringResource(Res.string.simulator_name_lmu)
    is Simulator.Gt7Ps5 -> stringResource(Res.string.simulator_name_gt7_ps5)
}

@Composable
private fun simulatorIcon(simulator: Simulator) = when (simulator) {
    is Simulator.Gt7Ps5 -> painterResource(Res.drawable.gt7)
    is Simulator.LmuWindows -> painterResource(Res.drawable.lmu)
}

@Composable
private fun itemDisplayName(itemId: ReadoutItemKey): String = when (itemId) {
    is ReadoutItemKey.VehicleApproach -> stringResource(Res.string.item_vehicle_approach)
    is ReadoutItemKey.Flag -> stringResource(Res.string.item_flag)
    is ReadoutItemKey.BlueFlag -> stringResource(Res.string.item_blue_flag)
    is ReadoutItemKey.SectorYellowFlag -> stringResource(Res.string.item_sector_yellow_flag)
    is ReadoutItemKey.FullCourseYellow -> stringResource(Res.string.item_full_course_yellow)
    is ReadoutItemKey.RedFlag -> stringResource(Res.string.item_red_flag)
    is ReadoutItemKey.VehicleDamage -> stringResource(Res.string.item_vehicle_damage)
    is ReadoutItemKey.Overheat -> stringResource(Res.string.item_overheat)
    is ReadoutItemKey.MyBestLap -> stringResource(Res.string.item_my_best_lap)
    is ReadoutItemKey.RemainingFuelLaps -> stringResource(Res.string.item_remaining_fuel_laps)
}

private fun itemIcon(itemId: ReadoutItemKey): ImageVector = when (itemId) {
    is ReadoutItemKey.VehicleApproach -> Icons.Filled.DirectionsCar
    is ReadoutItemKey.Flag -> Icons.Filled.Flag
    is ReadoutItemKey.BlueFlag -> Icons.Filled.Flag
    is ReadoutItemKey.SectorYellowFlag -> Icons.Filled.Flag
    is ReadoutItemKey.FullCourseYellow -> Icons.Filled.Flag
    is ReadoutItemKey.RedFlag -> Icons.Filled.Flag
    is ReadoutItemKey.VehicleDamage -> Icons.Filled.Build
    is ReadoutItemKey.Overheat -> Icons.Filled.Build
    is ReadoutItemKey.MyBestLap -> Icons.Filled.Timer
    is ReadoutItemKey.RemainingFuelLaps -> Icons.Filled.LocalGasStation
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PriorityHintRow() {
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
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp),
            )
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(bottom = 12.dp),
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
    onItemClick: (ReadoutItemKey) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        onMove(from.index, to.index)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
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
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .testTag("simulator_dropdown_trigger"),
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
                        modifier = Modifier.testTag("simulator_item_${simulator.id}"),
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = uiState.selectedSimulator != null,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 400),
            ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                PriorityHintRow()
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    itemsIndexed(uiState.items, key = { _, it -> it.value }) { index, item ->
                        ReorderableItem(reorderableState, key = item.value) {
                            val isSelected = uiState.selectedSimulator?.let {
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
                            ElevatedCard(
                                onClick = { onItemClick(item) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("readout_item_$index"),
                                colors = CardDefaults.elevatedCardColors(containerColor = cardContainerColor),
                            ) {
                                ListItem(
                                    headlineContent = { Text(itemDisplayName(item)) },
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
                                        Switch(
                                            checked = uiState.readoutEnabledStates[item] != false,
                                            onCheckedChange = { onReadoutEnabledChanged(item, it) },
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
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
        onItemClick = { _ -> },
    )
}

private class ReadoutListPanePreviewParameterProvider : PreviewParameterProvider<ReadoutListUiState> {
    override val values: Sequence<ReadoutListUiState> = sequenceOf(
        ReadoutListUiState(
            simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
            selectedSimulator = Simulator.LmuWindows,
            items = listOf(
                ReadoutItemKey.VehicleApproach,
                ReadoutItemKey.Flag,
                ReadoutItemKey.VehicleDamage,
            ),
        ),
        ReadoutListUiState(
            simulators = listOf(Simulator.LmuWindows, Simulator.Gt7Ps5),
            selectedSimulator = Simulator.Gt7Ps5,
            items = listOf(
                ReadoutItemKey.MyBestLap,
                ReadoutItemKey.RemainingFuelLaps,
            ),
        ),
    )
}
