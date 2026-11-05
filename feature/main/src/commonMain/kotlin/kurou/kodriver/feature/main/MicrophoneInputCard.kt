package kurou.kodriver.feature.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.microphone_input_device_label
import kurou.kodriver.feature.main.generated.resources.microphone_input_device_name
import kurou.kodriver.feature.main.generated.resources.microphone_input_device_not_found
import kurou.kodriver.feature.main.generated.resources.microphone_input_level_description
import kurou.kodriver.feature.main.generated.resources.microphone_input_no_signal_guidance
import kurou.kodriver.feature.main.generated.resources.microphone_input_status_capturing
import kurou.kodriver.feature.main.generated.resources.microphone_input_status_no_signal
import kurou.kodriver.feature.main.generated.resources.microphone_input_status_starting
import kurou.kodriver.feature.main.generated.resources.microphone_input_status_stopped
import kurou.kodriver.feature.main.generated.resources.microphone_input_status_unavailable
import kurou.kodriver.feature.main.generated.resources.microphone_input_switch_description
import kurou.kodriver.feature.main.generated.resources.microphone_input_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/** マイクの入力デバイス名・状態・音量の確認と、取得の開始・停止を行う簡易カード。Windows 向け。 */
@Composable
internal fun MicrophoneInputCard(
    uiState: MicrophoneInputUiState,
    onEnabledChange: (Boolean) -> Unit,
    onDeviceMenuOpen: () -> Unit,
    onDeviceSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val switchDescription = stringResource(Res.string.microphone_input_switch_description)
    val levelDescription = stringResource(Res.string.microphone_input_level_description)
    Surface(
        modifier =
            modifier.fillMaxWidth().padding(
                horizontal = KoDriverSpacing.small,
                vertical = KoDriverSpacing.small,
            ),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(KoDriverSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint =
                        if (uiState.enabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.microphone_input_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(uiState.status.label()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.enabled,
                    onCheckedChange = onEnabledChange,
                    modifier = Modifier.semantics { contentDescription = switchDescription },
                )
            }
            // デバイス一覧の取得が終わる前（準備中）は、見つからないと誤解させないよう何も表示しない
            val deviceEnumerated =
                uiState.status != MicrophoneInputStatus.Stopped && uiState.status != MicrophoneInputStatus.Starting
            val deviceLine =
                uiState.deviceName?.let { stringResource(Res.string.microphone_input_device_name, it) }
                    ?: stringResource(Res.string.microphone_input_device_not_found).takeIf { deviceEnumerated }
            if (uiState.devices.isNotEmpty() && uiState.deviceName != null) {
                MicrophoneDeviceDropdown(
                    devices = uiState.devices,
                    selected = uiState.deviceName,
                    onMenuOpen = onDeviceMenuOpen,
                    onSelected = onDeviceSelected,
                )
            } else if (deviceLine != null) {
                Text(
                    text = deviceLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (uiState.status == MicrophoneInputStatus.Capturing) {
                LinearProgressIndicator(
                    progress = { uiState.level },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = levelDescription },
                )
            }
            if (uiState.status == MicrophoneInputStatus.NoSignal) {
                Text(
                    text = stringResource(Res.string.microphone_input_no_signal_guidance),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MicrophoneDeviceDropdown(
    devices: List<String>,
    selected: String,
    onMenuOpen: () -> Unit,
    onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            if (it) onMenuOpen()
            expanded = it
        },
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(Res.string.microphone_input_device_label)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .testTag("microphone_input_device"),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            devices.forEach { name ->
                DropdownMenuItem(
                    text = { Text(name) },
                    modifier = Modifier.testTag("microphone_input_device_$name"),
                    onClick = {
                        onSelected(name)
                        expanded = false
                    },
                )
            }
        }
    }
}

private fun MicrophoneInputStatus.label(): StringResource =
    when (this) {
        MicrophoneInputStatus.Stopped -> Res.string.microphone_input_status_stopped
        MicrophoneInputStatus.Starting -> Res.string.microphone_input_status_starting
        MicrophoneInputStatus.Capturing -> Res.string.microphone_input_status_capturing
        MicrophoneInputStatus.Unavailable -> Res.string.microphone_input_status_unavailable
        MicrophoneInputStatus.NoSignal -> Res.string.microphone_input_status_no_signal
    }
