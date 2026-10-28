package kurou.kodriver.feature.manualreadoutdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneScaffold
import kurou.kodriver.core.designsystem.KoDriverDestructiveConfirmDialog
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.core.designsystem.rememberTextInsertState
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.acewindows.model.findUnknownAceWindowsMyBestLapReadoutPlaceholders
import kurou.kodriver.domain.acewindows.model.findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders
import kurou.kodriver.domain.acewindows.model.findUnknownAceWindowsRemainingFuelReadoutPlaceholders
import kurou.kodriver.domain.acewindows.model.findUnknownAceWindowsTyreTemperatureReadoutPlaceholders
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_MY_BEST_LAP_LAPTIME_PLACEHOLDER
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.gt7ps5.model.GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.gt7ps5.model.findUnknownGt7Ps5MyBestLapReadoutPlaceholders
import kurou.kodriver.domain.gt7ps5.model.findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders
import kurou.kodriver.domain.gt7ps5.model.findUnknownGt7Ps5RemainingFuelReadoutPlaceholders
import kurou.kodriver.domain.gt7ps5.model.findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsMyBestLapReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsPitTimingReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsRemainingFuelReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders
import kurou.kodriver.domain.model.AceWindowsReadoutItemKey
import kurou.kodriver.domain.model.Gt7Ps5ReadoutItemKey
import kurou.kodriver.domain.model.LmuWindowsReadoutItemKey
import kurou.kodriver.domain.model.ManualReadoutHotkeyModifiers
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.domain.model.isLmuWindowsManualReadoutTemperatureItem
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.Res
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_cancel
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_celsius_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_celsius_insert
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete_body
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete_confirm
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_delete_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_fuel_laps_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_laps_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_laps_insert
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_laptime_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_laptime_insert
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_no_items
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_percent_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_percent_insert
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_missing
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_set_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_blank
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_label
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_reset
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_text_unknown
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_title
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_tyre_wear_laps_hint
import kurou.kodriver.feature.manualreadoutdetail.generated.resources.manual_readout_virtual_energy_laps_hint
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val SIMHUB_HOTKEY_SETUP_URL =
    "https://github.com/ai-kurou/KoDriver/blob/main/docs/simhub-hotkey-setup.md"

@Composable
fun ManualReadoutSetDetailContent(
    simulatorId: String,
    setId: String,
    onDeleted: () -> Unit,
    canNavigateBack: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    showHotkey: Boolean = isManualReadoutHotkeySupported(),
    showSimHubGuideLink: Boolean = simulatorId != Simulator.Gt7Ps5.id,
) {
    val viewModel: ManualReadoutSetDetailViewModel =
        koinViewModel(key = "$simulatorId:$setId", parameters = { parametersOf(simulatorId, setId) })
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    val currentOnDeleted by rememberUpdatedState(onDeleted)
    SideEffect(uiState.deleted) {
        if (uiState.deleted) currentOnDeleted()
    }
    ManualReadoutSetDetailPane(
        uiState = uiState,
        onAssignHotkey = viewModel::assignHotkey,
        onSelectModifiers = viewModel::selectHotkeyModifiers,
        onClearHotkey = viewModel::clearHotkey,
        onRetryHotkeyRegistration = viewModel::onRetryHotkeyRegistration,
        showHotkey = showHotkey,
        showSimHubGuideLink = showSimHubGuideLink,
        onAddItem = viewModel::addItem,
        onRemoveItem = viewModel::removeItem,
        onMoveItem = viewModel::moveItem,
        onDelete = viewModel::deleteSet,
        onItemTextChanged = viewModel::updateItemText,
        onNameChanged = viewModel::updateName,
        onPreviewClick = viewModel::onPreviewClicked,
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
internal fun ManualReadoutSetDetailPane(
    uiState: ManualReadoutSetDetailUiState,
    onAddItem: (String) -> Unit,
    onDelete: () -> Unit,
    canNavigateBack: Boolean = true,
    onBack: () -> Unit = {},
    onRemoveItem: (String) -> Unit = {},
    onMoveItem: (Int, Int) -> Unit = { _, _ -> },
    onItemTextChanged: (String, String) -> Unit = { _, _ -> },
    onNameChanged: (String) -> Unit = {},
    onPreviewClick: () -> Unit = {},
    onAssignHotkey: (Int) -> Unit = {},
    onSelectModifiers: (ManualReadoutHotkeyModifiers) -> Unit = {},
    onClearHotkey: () -> Unit = {},
    onRetryHotkeyRegistration: () -> Unit = {},
    modifier: Modifier = Modifier,
    showHotkey: Boolean = isManualReadoutHotkeySupported(),
    showSimHubGuideLink: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    if (showDeleteDialog) {
        ManualReadoutSetDeleteConfirmDialog(
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false },
        )
    }
    val number = uiState.setNumber
    DetailPaneScaffold(
        title =
            if (number != null) {
                uiState.name.ifBlank { stringResource(Res.string.manual_readout_set_title, number) }
            } else {
                stringResource(Res.string.manual_readout_title)
            },
        canNavigateBack = canNavigateBack,
        onBack = onBack,
        modifier = modifier,
        actions = {
            if (number != null) {
                val haptic = LocalHapticFeedback.current
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                        showDeleteDialog = true
                    },
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(Res.string.manual_readout_delete))
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(KoDriverSpacing.large),
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.medium),
        ) {
            if (number != null) {
                ManualReadoutSetNameRow(uiState.name, number, onNameChanged)
                if (uiState.availableItemKeys.isEmpty()) {
                    Text(stringResource(Res.string.manual_readout_no_items))
                }
                if (uiState.availableItemKeys.isNotEmpty()) {
                    ManualReadoutItemsCard(
                        uiState,
                        onAddItem,
                        onRemoveItem,
                        onMoveItem,
                        onItemTextChanged,
                        onPreviewClick,
                    )
                }
                if (showHotkey) {
                    ManualReadoutHotkeyCard(
                        uiState,
                        onAssignHotkey,
                        onClearHotkey,
                        onRetryHotkeyRegistration,
                        onSelectModifiers,
                        onOpenGuide = { uriHandler.openUri(SIMHUB_HOTKEY_SETUP_URL) },
                        showGuideLink = showSimHubGuideLink,
                    )
                }
            } else if (!uiState.isLoading) {
                Text(stringResource(Res.string.manual_readout_set_missing))
            }
        }
    }
}

@Composable
internal fun ManualReadoutSetDeleteConfirmDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    KoDriverDestructiveConfirmDialog(
        title = stringResource(Res.string.manual_readout_delete_title),
        body = stringResource(Res.string.manual_readout_delete_body),
        confirmLabel = stringResource(Res.string.manual_readout_delete_confirm),
        dismissLabel = stringResource(Res.string.manual_readout_cancel),
        onConfirm = onConfirm,
        onDismiss = onDismiss,
    )
}

internal data class ManualReadoutPlaceholderDefinition(
    val insertText: String,
    val findUnknown: (String) -> List<String>,
)

internal val manualReadoutPlaceholderDefinitions =
    mapOf(
        (Simulator.AceWindows.id to AceWindowsReadoutItemKey.RemainingFuel.Root.value) to
            ManualReadoutPlaceholderDefinition(
                ACE_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER,
                ::findUnknownAceWindowsRemainingFuelReadoutPlaceholders,
            ),
        (Simulator.AceWindows.id to AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value) to
            ManualReadoutPlaceholderDefinition(
                ACE_WINDOWS_REMAINING_FUEL_LAPS_PLACEHOLDER,
                ::findUnknownAceWindowsRemainingFuelLapsReadoutPlaceholders,
            ),
        (Simulator.AceWindows.id to AceWindowsReadoutItemKey.MyBestLap.Root.value) to
            ManualReadoutPlaceholderDefinition(
                ACE_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
                ::findUnknownAceWindowsMyBestLapReadoutPlaceholders,
            ),
        (Simulator.AceWindows.id to AceWindowsReadoutItemKey.TyreTemperature.Root.value) to
            ManualReadoutPlaceholderDefinition(
                ACE_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                ::findUnknownAceWindowsTyreTemperatureReadoutPlaceholders,
            ),
        (Simulator.Gt7Ps5.id to Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value) to
            ManualReadoutPlaceholderDefinition(
                GT7_PS5_REMAINING_FUEL_LAPS_PLACEHOLDER,
                ::findUnknownGt7Ps5RemainingFuelLapsReadoutPlaceholders,
            ),
        (Simulator.Gt7Ps5.id to Gt7Ps5ReadoutItemKey.RemainingFuel.Root.value) to
            ManualReadoutPlaceholderDefinition(
                GT7_PS5_REMAINING_FUEL_PERCENT_PLACEHOLDER,
                ::findUnknownGt7Ps5RemainingFuelReadoutPlaceholders,
            ),
        (Simulator.Gt7Ps5.id to Gt7Ps5ReadoutItemKey.MyBestLap.Root.value) to
            ManualReadoutPlaceholderDefinition(
                GT7_PS5_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
                ::findUnknownGt7Ps5MyBestLapReadoutPlaceholders,
            ),
        (Simulator.Gt7Ps5.id to Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value) to
            ManualReadoutPlaceholderDefinition(
                GT7_PS5_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                ::findUnknownGt7Ps5TyreTemperatureReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
                ::findUnknownLmuWindowsPitTimingReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.RemainingFuel.Laps.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
                ::findUnknownLmuWindowsPitTimingReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.PitTiming.TyreWear.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_PIT_TIMING_LAPS_PLACEHOLDER,
                ::findUnknownLmuWindowsPitTimingReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.MyBestLap.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_MY_BEST_LAP_LAPTIME_PLACEHOLDER,
                ::findUnknownLmuWindowsMyBestLapReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.RemainingFuel.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER,
                ::findUnknownLmuWindowsRemainingFuelReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.RemainingVirtualEnergy.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
                ::findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.TyreWear.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
                ::findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.BrakeWear.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_REMAINING_VIRTUAL_ENERGY_PERCENT_PLACEHOLDER,
                ::findUnknownLmuWindowsRemainingVirtualEnergyReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.TyreTemperature.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                ::findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders,
            ),
        (Simulator.LmuWindows.id to LmuWindowsReadoutItemKey.BrakeTemperature.Root.value) to
            ManualReadoutPlaceholderDefinition(
                LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                ::findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders,
            ),
    )

private fun manualReadoutPlaceholderDefinition(
    simulatorId: String,
    itemKey: String,
): ManualReadoutPlaceholderDefinition? = manualReadoutPlaceholderDefinitions[simulatorId to itemKey]

internal fun unknownPlaceholders(
    simulatorId: String,
    itemKey: String,
    text: String,
): List<String> = manualReadoutPlaceholderDefinition(simulatorId, itemKey)?.findUnknown?.invoke(text).orEmpty()

private fun isLmuWindowsManualReadoutLapsItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value ||
        itemKey == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value ||
        itemKey == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value

internal fun insertPlaceholder(
    simulatorId: String,
    itemKey: String,
): String = manualReadoutPlaceholderDefinition(simulatorId, itemKey)?.insertText.orEmpty()

/** 選択中の項目の文言を編集し、入力ごとに保存する。 */
private fun lapsHintResource(itemKey: String): StringResource =
    if (itemKey == LmuWindowsReadoutItemKey.PitTiming.VirtualEnergy.value) {
        Res.string.manual_readout_virtual_energy_laps_hint
    } else if (itemKey == LmuWindowsReadoutItemKey.RemainingFuel.Laps.value) {
        Res.string.manual_readout_fuel_laps_hint
    } else if (itemKey == LmuWindowsReadoutItemKey.PitTiming.TyreWear.value) {
        Res.string.manual_readout_tyre_wear_laps_hint
    } else {
        Res.string.manual_readout_laps_hint
    }

private fun isManualReadoutLapTimeItem(itemKey: String): Boolean =
    itemKey == LmuWindowsReadoutItemKey.MyBestLap.Root.value ||
        itemKey == Gt7Ps5ReadoutItemKey.MyBestLap.Root.value ||
        itemKey == AceWindowsReadoutItemKey.MyBestLap.Root.value

private fun isManualReadoutLapsItem(itemKey: String): Boolean =
    itemKey == Gt7Ps5ReadoutItemKey.RemainingFuelLaps.Root.value ||
        itemKey == AceWindowsReadoutItemKey.RemainingFuelLaps.Root.value ||
        isLmuWindowsManualReadoutLapsItem(itemKey)

private fun isManualReadoutTemperatureItem(itemKey: String): Boolean =
    itemKey == AceWindowsReadoutItemKey.TyreTemperature.Root.value ||
        itemKey == Gt7Ps5ReadoutItemKey.TyreTemperature.Root.value ||
        isLmuWindowsManualReadoutTemperatureItem(itemKey)

@Composable
internal fun ManualReadoutItemTextField(
    text: String,
    defaultText: String,
    onTextChanged: (String) -> Unit,
    itemKey: String,
    simulatorId: String,
) {
    val haptic = LocalHapticFeedback.current
    val isLapTime = isManualReadoutLapTimeItem(itemKey)
    val isLaps = isManualReadoutLapsItem(itemKey)
    val isTemperature = isManualReadoutTemperatureItem(itemKey)
    val textState = rememberPendingText(text, READOUT_CUSTOM_TEXT_MAX_LENGTH)
    val currentText = textState.currentText
    val insertState = rememberTextInsertState()
    val changeText: (String) -> Unit = {
        textState.change(it)
        onTextChanged(it)
    }
    val unknown = unknownPlaceholders(simulatorId, itemKey, currentText).joinToString("、")
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small)) {
        DetailPaneLabeledTextField(
            label = stringResource(Res.string.manual_readout_text_label),
            value = currentText,
            insertState = insertState,
            defaultValue = defaultText,
            maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
            onValueChangeFinished = changeText,
            onPreviewClick = null,
            onResetToDefault = { changeText(defaultText) },
            resetContentDescription = stringResource(Res.string.manual_readout_text_reset),
            supportingText =
                when {
                    unknown.isNotEmpty() -> stringResource(Res.string.manual_readout_text_unknown, unknown)
                    currentText.isBlank() -> stringResource(Res.string.manual_readout_text_blank)
                    else -> null
                },
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
        ) {
            AssistChip(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                    insertState.insert(insertPlaceholder(simulatorId, itemKey))
                },
                label = {
                    Text(
                        stringResource(
                            if (isLapTime) {
                                Res.string.manual_readout_laptime_insert
                            } else if (isLaps) {
                                Res.string.manual_readout_laps_insert
                            } else if (isTemperature) {
                                Res.string.manual_readout_celsius_insert
                            } else {
                                Res.string.manual_readout_percent_insert
                            },
                        ),
                    )
                },
                // 押下で入力欄のフォーカスが外れると選択範囲が解除されるため、チップはフォーカスを取らない。
                modifier = Modifier.focusProperties { canFocus = false },
            )
            Text(
                text =
                    stringResource(
                        if (isLapTime) {
                            Res.string.manual_readout_laptime_hint
                        } else if (isLaps) {
                            lapsHintResource(itemKey)
                        } else if (isTemperature) {
                            Res.string.manual_readout_celsius_hint
                        } else {
                            Res.string.manual_readout_percent_hint
                        },
                    ),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
