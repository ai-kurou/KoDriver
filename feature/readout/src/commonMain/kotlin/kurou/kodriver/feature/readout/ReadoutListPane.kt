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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kodriver.feature.readout.generated.resources.Res
import kodriver.feature.readout.generated.resources.lmu
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadoutListPane(
    uiState: ReadoutListUiState,
    onSimulatorSelected: (String) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onSwitchChanged: (String, Boolean) -> Unit,
    onItemClick: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

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
                value = uiState.selectedSimulator ?: "シミュレータを選択",
                onValueChange = {},
                readOnly = true,
                label = { Text("シミュレーター") },
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
                        text = { Text(simulator) },
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(uiState.items, key = { it }) { label ->
                val index = uiState.items.indexOf(label)
                ElevatedCard(
                    onClick = onItemClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .animateItem(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    ),
                ) {
                    ListItem(
                        headlineContent = { Text(label) },
                        leadingContent = {
                            Column {
                                IconButton(
                                    onClick = { onMoveUp(index) },
                                    enabled = index > 0,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowUp,
                                        contentDescription = "上に移動",
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveDown(index) },
                                    enabled = index < uiState.items.lastIndex,
                                    modifier = Modifier.size(36.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = "下に移動",
                                    )
                                }
                            }
                        },
                        trailingContent = {
                            Switch(
                                checked = uiState.switchStates[label] != false,
                                onCheckedChange = { onSwitchChanged(label, it) },
                            )
                        },
                    )
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
            simulators = listOf("Le Mans Ultimate"),
            selectedSimulator = "Le Mans Ultimate",
            items = listOf("車両接近", "残りラップ数"),
        ),
        onSimulatorSelected = {},
        onMoveUp = {},
        onMoveDown = {},
        onSwitchChanged = { _, _ -> },
        onItemClick = {},
    )
}
