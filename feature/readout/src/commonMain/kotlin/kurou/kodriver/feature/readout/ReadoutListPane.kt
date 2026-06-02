package kurou.kodriver.feature.readout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

private val simulators = listOf("Le Mans Ultimate")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadoutListPane(onItemClick: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf(simulators[0]) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
        ) {
            OutlinedTextField(
                value = selected,
                onValueChange = {},
                readOnly = true,
                label = { Text("シミュレーター") },
                leadingIcon = {
                    Image(
                        painter = painterResource(Res.drawable.lmu),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                simulators.forEach { simulator ->
                    DropdownMenuItem(
                        text = { Text(simulator) },
                        onClick = {
                            selected = simulator
                            expanded = false
                            onItemClick()
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
        LazyColumn(modifier = Modifier.fillMaxSize()) {}
    }
}

@Preview
@Composable
fun ReadoutListPanePreview() {
    ReadoutListPane(onItemClick = {})
}
