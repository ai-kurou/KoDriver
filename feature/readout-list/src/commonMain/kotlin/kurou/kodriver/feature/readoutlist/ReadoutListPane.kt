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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Flag
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.readoutlist.generated.resources.Res
import kodriver.feature.readoutlist.generated.resources.drag_handle
import kodriver.feature.readoutlist.generated.resources.gt7
import kodriver.feature.readoutlist.generated.resources.item_best_lap
import kodriver.feature.readoutlist.generated.resources.item_flag
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
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
private fun simulatorDisplayName(simulatorId: String): String = when (simulatorId) {
    "lmu_windows" -> stringResource(Res.string.simulator_name_lmu)
    "gt7_ps5" -> stringResource(Res.string.simulator_name_gt7_ps5)
    else -> simulatorId
}

@Composable
private fun simulatorIcon(simulatorId: String) = when (simulatorId) {
    "gt7_ps5" -> painterResource(Res.drawable.gt7)
    else -> painterResource(Res.drawable.lmu)
}

@Composable
private fun itemDisplayName(itemId: ReadoutItemKey): String = when (itemId) {
    ReadoutItemKey.VEHICLE_APPROACH -> stringResource(Res.string.item_vehicle_approach)
    ReadoutItemKey.FLAG -> stringResource(Res.string.item_flag)
    ReadoutItemKey.VEHICLE_DAMAGE -> stringResource(Res.string.item_vehicle_damage)
    ReadoutItemKey.BEST_LAP -> stringResource(Res.string.item_best_lap)
    else -> itemId.value
}

private fun itemIcon(itemId: ReadoutItemKey): ImageVector = when (itemId) {
    ReadoutItemKey.VEHICLE_APPROACH -> Icons.Filled.DirectionsCar
    ReadoutItemKey.FLAG -> Icons.Filled.Flag
    ReadoutItemKey.VEHICLE_DAMAGE -> Icons.Filled.Build
    ReadoutItemKey.BEST_LAP -> Icons.Filled.Timer
    else -> Icons.Filled.DirectionsCar
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
    onSimulatorSelected: (String) -> Unit,
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
                            modifier = Modifier.size(24.dp),
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
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        modifier = Modifier.testTag("simulator_item_$simulator"),
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
private fun ReadoutListPanePreview() {
    ReadoutListPane(
        uiState = ReadoutListUiState(
            simulators = listOf("lmu_windows"),
            selectedSimulator = "lmu_windows",
            items = listOf(
                ReadoutItemKey.VEHICLE_APPROACH,
                ReadoutItemKey.FLAG,
                ReadoutItemKey.VEHICLE_DAMAGE,
            ),
        ),
        onSimulatorSelected = {},
        onMove = { _, _ -> },
        onReadoutEnabledChanged = { _, _ -> },
        onItemClick = { _ -> },
    )
}
