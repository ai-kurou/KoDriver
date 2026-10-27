package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.domain.model.HotkeyRegistrationState
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_clear
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_guide_link
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_none
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_other_set
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_other_set_named
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_conflict
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_failed
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_registered
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_registering
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_registration_retry
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_hotkey_title
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ManualReadoutHotkeyCard(
    state: ManualReadoutSetDetailUiState,
    onAssign: (Int) -> Unit,
    onClear: () -> Unit,
    onRetry: () -> Unit,
    onSelectModifiers: (ManualReadoutHotkeyModifiers) -> Unit,
    onOpenGuide: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    DetailPaneCard(
        title = stringResource(Res.string.manual_readout_hotkey_title),
        modifier = Modifier.testTag("manual_readout_hotkey_card"),
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                Row(horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
                    ManualReadoutHotkeyModifiersDropdown(state.hotkeyModifiers, onSelectModifiers)
                    OutlinedTextField(
                        value =
                            state.hotkeyVirtualKeyCode?.let { manualReadoutHotkeyDisplayName(it) }
                                ?: stringResource(Res.string.manual_readout_hotkey_none),
                        onValueChange = {},
                        readOnly = true,
                        modifier =
                            Modifier
                                .weight(1f)
                                .testTag("manual_readout_hotkey_key")
                                .onPreviewKeyEvent { event ->
                                    val code = manualReadoutHotkeyVirtualKeyCode(event.key)
                                    if (event.type == KeyEventType.KeyDown && code != null) {
                                        onAssign(code)
                                        true
                                    } else {
                                        false
                                    }
                                },
                    )
                }
                val conflictText =
                    when (val conflict = state.hotkeyConflict) {
                        is ManualReadoutHotkeyConflict.OtherSet -> {
                            if (conflict.name.isBlank()) {
                                stringResource(
                                    Res.string.manual_readout_hotkey_other_set,
                                    simulatorDisplayName(conflict.simulatorId),
                                    conflict.setNumber,
                                )
                            } else {
                                stringResource(
                                    Res.string.manual_readout_hotkey_other_set_named,
                                    simulatorDisplayName(conflict.simulatorId),
                                    conflict.name,
                                )
                            }
                        }

                        null -> {
                            null
                        }
                    }
                if (conflictText != null) Text(conflictText, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onClear, enabled = state.hotkeyVirtualKeyCode != null) {
                    Text(stringResource(Res.string.manual_readout_hotkey_clear))
                }
                if (state.hotkeyVirtualKeyCode != null) {
                    ManualReadoutHotkeyRegistration(state.hotkeyRegistrationState, onRetry)
                }
                Text(stringResource(Res.string.manual_readout_hotkey_hint))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                            onOpenGuide()
                        },
                ) {
                    Text(
                        text = stringResource(Res.string.manual_readout_hotkey_guide_link),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.size(KoDriverSpacing.extraSmall))
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }
        },
    )
}

@Composable
private fun ManualReadoutHotkeyRegistration(
    registration: HotkeyRegistrationState,
    onRetry: () -> Unit,
) {
    when (registration) {
        HotkeyRegistrationState.Unsupported -> {
            Unit
        }

        HotkeyRegistrationState.Registering -> {
            Text(stringResource(Res.string.manual_readout_hotkey_registration_registering))
        }

        HotkeyRegistrationState.Registered -> {
            Text(stringResource(Res.string.manual_readout_hotkey_registration_registered))
        }

        HotkeyRegistrationState.Conflict -> {
            Text(stringResource(Res.string.manual_readout_hotkey_registration_conflict))
        }

        is HotkeyRegistrationState.Failed -> {
            Text(
                stringResource(Res.string.manual_readout_hotkey_registration_failed, registration.errorCode),
            )
        }
    }
    if (registration is HotkeyRegistrationState.Conflict || registration is HotkeyRegistrationState.Failed) {
        TextButton(onClick = onRetry) {
            Text(stringResource(Res.string.manual_readout_hotkey_registration_retry))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManualReadoutHotkeyModifiersDropdown(
    modifiers: ManualReadoutHotkeyModifiers,
    onSelect: (ManualReadoutHotkeyModifiers) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = modifiers.displayName(),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier =
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .testTag("manual_readout_hotkey_modifiers"),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ManualReadoutHotkeyModifiers.entries.forEach { value ->
                DropdownMenuItem(
                    text = { Text(value.displayName()) },
                    modifier = Modifier.testTag("manual_readout_hotkey_modifiers_${value.name}"),
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                )
            }
        }
    }
}
