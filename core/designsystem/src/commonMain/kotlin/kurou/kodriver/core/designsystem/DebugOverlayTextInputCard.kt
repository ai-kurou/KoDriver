package kurou.kodriver.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kurou.kodriver.core.designsystem.generated.resources.Res
import kurou.kodriver.core.designsystem.generated.resources.debug_overlay_text_input_label
import kurou.kodriver.core.designsystem.generated.resources.debug_overlay_text_input_readout_item
import kurou.kodriver.core.designsystem.generated.resources.debug_overlay_text_input_send
import kurou.kodriver.core.designsystem.generated.resources.debug_overlay_text_input_title
import org.jetbrains.compose.resources.stringResource

/** 音声を鳴らさずオーバーレイへ表示する文言と、読み上げ項目の識別子を入力する。 */
@Suppress("UnstableCollections")
@Composable
fun DebugOverlayTextInputCard(
    text: String,
    selectedKey: String,
    keys: List<String>,
    onTextChange: (String) -> Unit,
    onKeySelect: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    DetailPaneCard(
        title = stringResource(Res.string.debug_overlay_text_input_title),
        modifier = modifier,
        bottomContent = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
            ) {
                Text(stringResource(Res.string.debug_overlay_text_input_readout_item))
                Box {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text("${readoutItemDisplayName(selectedKey)} ($selectedKey)")
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        keys.forEach { key ->
                            DropdownMenuItem(
                                text = { Text("${readoutItemDisplayName(key)} ($key)") },
                                onClick = {
                                    expanded = false
                                    onKeySelect(key)
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChange,
                    label = { Text(stringResource(Res.string.debug_overlay_text_input_label)) },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = onSend, enabled = text.isNotBlank()) {
                    Text(stringResource(Res.string.debug_overlay_text_input_send))
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun DebugOverlayTextInputCardPreview() {
    KoDriverTheme {
        DebugOverlayTextInputCard(
            text = "オーバーレイ表示テスト",
            selectedKey = "gt7_ps5_my_best_lap",
            keys = listOf("gt7_ps5_my_best_lap", "gt7_ps5_remaining_fuel"),
            onTextChange = {},
            onKeySelect = {},
            onSend = {},
        )
    }
}
