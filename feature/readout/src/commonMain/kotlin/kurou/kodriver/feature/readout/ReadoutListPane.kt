package kurou.kodriver.feature.readout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.readout.generated.resources.Res
import kodriver.feature.readout.generated.resources.drag_handle
import kodriver.feature.readout.generated.resources.item_laps_remaining
import kodriver.feature.readout.generated.resources.item_vehicle_approach
import kodriver.feature.readout.generated.resources.lmu
import kodriver.feature.readout.generated.resources.select_simulator_hint
import kodriver.feature.readout.generated.resources.simulator_label
import kodriver.feature.readout.generated.resources.simulator_name_lmu
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
private fun simulatorDisplayName(simulatorId: String): String = when (simulatorId) {
    "lmu" -> stringResource(Res.string.simulator_name_lmu)
    else -> simulatorId
}

@Composable
private fun itemDisplayName(itemId: String): String = when (itemId) {
    "vehicle_approach" -> stringResource(Res.string.item_vehicle_approach)
    "laps_remaining" -> stringResource(Res.string.item_laps_remaining)
    else -> itemId
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadoutListPane(
    uiState: ReadoutListUiState,
    onSimulatorSelected: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onReadoutEnabledChanged: (String, Boolean) -> Unit,
    onItemClick: () -> Unit,
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
                            painter = painterResource(Res.drawable.lmu),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                } else {
                    null
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
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
                                painter = painterResource(Res.drawable.lmu),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
        ) {
            items(uiState.items, key = { it }) { item ->
                ReorderableItem(reorderableState, key = item) {
                    ElevatedCard(
                        onClick = onItemClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ) {
                        ListItem(
                            headlineContent = { Text(itemDisplayName(item)) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Filled.DragIndicator,
                                    contentDescription = stringResource(Res.string.drag_handle),
                                    modifier = Modifier.draggableHandle(),
                                )
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

@Preview
@Composable
fun ReadoutListPanePreview() {
    ReadoutListPane(
        uiState = ReadoutListUiState(
            simulators = listOf("lmu"),
            selectedSimulator = "lmu",
            items = listOf("vehicle_approach", "laps_remaining"),
        ),
        onSimulatorSelected = {},
        onMove = { _, _ -> },
        onReadoutEnabledChanged = { _, _ -> },
        onItemClick = {},
    )
}
