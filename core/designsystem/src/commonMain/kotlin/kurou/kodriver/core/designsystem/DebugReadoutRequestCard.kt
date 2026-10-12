package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.debug_readout_request_item
import kurou.kodriver.core.designsystem.generated.resources.debug_readout_request_send
import kurou.kodriver.core.designsystem.generated.resources.debug_readout_request_text
import kurou.kodriver.core.designsystem.generated.resources.debug_readout_request_title
import org.jetbrains.compose.resources.stringResource

/** 項目の文字列 ID と入力文言を渡して、Narrator へデバッグ読み上げを要求するカード。 */
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UnstableCollections")
@Composable
fun DebugReadoutRequestCard(
    readoutItemKeyValues: List<String>,
    selectedReadoutItemKeyValue: String,
    text: String,
    onReadoutItemSelected: (String) -> Unit,
    onTextChange: (String) -> Unit,
    onReadout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    DetailPaneCard(
        title = stringResource(Res.string.debug_readout_request_title),
        modifier = modifier.testTag("debug_readout_request_card"),
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = readoutItemDisplayName(selectedReadoutItemKeyValue),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(Res.string.debug_readout_request_item)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .testTag("debug_readout_request_item"),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        readoutItemKeyValues.forEach { value ->
                            DropdownMenuItem(
                                modifier = Modifier.testTag("debug_readout_item_$value"),
                                text = { Text(readoutItemDisplayName(value)) },
                                onClick = {
                                    onReadoutItemSelected(value)
                                    expanded = false
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text(stringResource(Res.string.debug_readout_request_text)) },
                    modifier = Modifier.fillMaxWidth().testTag("debug_readout_request_text"),
                )
                Button(onClick = onReadout, enabled = text.isNotBlank()) {
                    Text(stringResource(Res.string.debug_readout_request_send))
                }
            }
        },
    )
}
