package kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.PlaceholderReadoutTextField
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.generated.resources.class_elms_lmp2
import kurou.kodriver.core.designsystem.generated.resources.class_hypercar
import kurou.kodriver.core.designsystem.generated.resources.class_lmgt3
import kurou.kodriver.core.designsystem.generated.resources.class_lmgte
import kurou.kodriver.core.designsystem.generated.resources.class_p2
import kurou.kodriver.core.designsystem.generated.resources.class_p3
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.SessionPhase
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_carcass_card_title
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_celsius_insert
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_celsius_placeholder_hint
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_label
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_preview
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_reset
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_selected_icon
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_supporting
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_unavailable
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_cold_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_description
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_label
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_reset
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_high_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_card_title
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_formation
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_garage
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_grid_walk
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phase_warm_up
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_help_description
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_low_warning_phases_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_celsius_insert
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_celsius_placeholder_hint
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_label
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_preview
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_reset
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_selected_icon
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_supporting
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_unavailable
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_overheat_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_threshold_help_description
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_threshold_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsautoreadout.tyretemperaturedetail.generated.resources.tyre_temperature_vehicle_class_target_subtitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kurou.kodriver.core.designsystem.generated.resources.Res as DesignSystemRes

/**
 * LmuWindowsAutoReadoutTyreTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsAutoReadoutTyreTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsAutoReadoutTyreTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
        uiState = uiState,
        onOverheatWarningEnabledChanged = viewModel::onOverheatWarningEnabledChanged,
        onOverheatReadoutTextChanged = viewModel::onOverheatReadoutTextChanged,
        onOverheatReadoutTextPreviewClicked = viewModel::onOverheatReadoutTextPreviewClicked,
        onColdReadoutTextChanged = viewModel::onColdReadoutTextChanged,
        onLowWarningEnabledChanged = viewModel::onLowWarningEnabledChanged,
        onLowWarningPhaseToggled = viewModel::onLowWarningPhaseToggled,
        onLowWarningPreviewClicked = viewModel::onLowWarningPreviewClicked,
        onVehicleClassSelected = viewModel::onVehicleClassSelected,
        onVehicleClassHighThresholdChanged = viewModel::onVehicleClassHighThresholdChanged,
        onVehicleClassHighThresholdReset = viewModel::onVehicleClassHighThresholdReset,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
    uiState: LmuWindowsAutoReadoutTyreTemperatureDetailUiState,
    onOverheatWarningEnabledChanged: (Boolean) -> Unit = {},
    onOverheatReadoutTextChanged: (String) -> Unit = {},
    onOverheatReadoutTextPreviewClicked: (String, Int) -> Unit = { _, _ -> },
    onLowWarningEnabledChanged: (Boolean) -> Unit = {},
    onLowWarningPhaseToggled: (SessionPhase) -> Unit = {},
    onColdReadoutTextChanged: (String) -> Unit = {},
    onLowWarningPreviewClicked: (String) -> Unit = {},
    onVehicleClassSelected: (LmuWindowsVehicleClassData) -> Unit = {},
    onVehicleClassHighThresholdChanged: (LmuWindowsVehicleClassData, Int) -> Unit = { _, _ -> },
    onVehicleClassHighThresholdReset: (LmuWindowsVehicleClassData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.tyre_temperature_description),
        )
        val helpIconContentDescription =
            stringResource(Res.string.tyre_temperature_threshold_help_icon_content_description)
        val labelTemplate = stringResource(Res.string.tyre_temperature_high_threshold_label)
        val lowWarningPhasesHelpIconContentDescription =
            stringResource(Res.string.tyre_temperature_low_warning_phases_help_icon_content_description)
        val phaseLabels =
            mapOf(
                SessionPhase.GARAGE to stringResource(Res.string.tyre_temperature_low_warning_phase_garage),
                SessionPhase.WARM_UP to stringResource(Res.string.tyre_temperature_low_warning_phase_warm_up),
                SessionPhase.GRID_WALK to stringResource(Res.string.tyre_temperature_low_warning_phase_grid_walk),
                SessionPhase.FORMATION to stringResource(Res.string.tyre_temperature_low_warning_phase_formation),
            )
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_carcass_card_title),
            checked = uiState.overheatWarningEnabled,
            onCheckedChange = onOverheatWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.tyre_temperature_overheat_text_label),
                        savedText = uiState.overheatReadoutText,
                        defaultText = LMU_WINDOWS_TYRE_TEMPERATURE_OVERHEAT_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onOverheatReadoutTextChanged,
                        onPreviewClick = { text ->
                            val celsius = uiState.selectedVehicleClassHighThresholdCelsius.value
                            onOverheatReadoutTextPreviewClicked(text, celsius)
                        },
                        placeholder = LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.tyre_temperature_overheat_celsius_insert),
                        placeholderHint = stringResource(Res.string.tyre_temperature_overheat_celsius_placeholder_hint),
                        resetContentDescription = stringResource(Res.string.tyre_temperature_overheat_text_reset),
                        previewContentDescription = stringResource(Res.string.tyre_temperature_overheat_text_preview),
                        selectedContentDescription =
                            stringResource(Res.string.tyre_temperature_overheat_text_selected_icon),
                        unavailableText = stringResource(Res.string.tyre_temperature_overheat_text_unavailable),
                        supportingText = stringResource(Res.string.tyre_temperature_overheat_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let {
                                    stringResource(Res.string.tyre_temperature_overheat_text_unknown_placeholders, it)
                                }
                        },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_vehicle_class_target_subtitle),
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
                        modifier =
                            Modifier.fillMaxWidth().padding(
                                horizontal = KoDriverSpacing.extraSmall,
                                vertical = KoDriverSpacing.extraSmall,
                            ),
                    ) {
                        listOf(
                            Triple(
                                LmuWindowsVehicleClassData.Hypercar,
                                DesignSystemRes.drawable.class_hypercar,
                                "Hypercar",
                            ),
                            Triple(LmuWindowsVehicleClassData.P2, DesignSystemRes.drawable.class_p2, "LMP2"),
                            Triple(
                                LmuWindowsVehicleClassData.P2Elms,
                                DesignSystemRes.drawable.class_elms_lmp2,
                                "ELMS LMP2",
                            ),
                            Triple(LmuWindowsVehicleClassData.P3, DesignSystemRes.drawable.class_p3, "LMP3"),
                            Triple(LmuWindowsVehicleClassData.Gte, DesignSystemRes.drawable.class_lmgte, "LMGTE"),
                            Triple(LmuWindowsVehicleClassData.Gt3, DesignSystemRes.drawable.class_lmgt3, "LMGT3"),
                        ).forEach { (vehicleClass, image, accessibleName) ->
                            val celsius = uiState.vehicleClassHighThresholdCelsius[vehicleClass]
                            if (celsius != null) {
                                VehicleClassImageOption(
                                    image = image,
                                    accessibleName = "$accessibleName、$celsius°C",
                                    celsius = celsius,
                                    selected = uiState.selectedVehicleClass == vehicleClass,
                                    enabled = uiState.overheatWarningEnabled,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                        onVehicleClassSelected(vehicleClass)
                                    },
                                    modifier = Modifier.width(VEHICLE_CLASS_OPTION_WIDTH),
                                )
                            }
                        }
                    }
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_high_threshold_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = helpIconContentDescription,
                                sheetContent = { TyreTemperatureThresholdHelpSheetContent() },
                            )
                        },
                    )
                    val highThresholdMin = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.value.toFloat()
                    val highThresholdMax = LMU_WINDOWS_TYRE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.value.toFloat()
                    ThresholdSlider(
                        value = uiState.selectedVehicleClassHighThresholdCelsius.value.toFloat(),
                        valueRange = highThresholdMin..highThresholdMax,
                        steps = (highThresholdMax - highThresholdMin).toInt() - 1,
                        labelFormatter = { labelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            onVehicleClassHighThresholdChanged(uiState.selectedVehicleClass, it.roundToInt())
                        },
                        defaultValue =
                            lmuWindowsVehicleClassTyreTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            ).value.toFloat(),
                        onResetToDefault = { onVehicleClassHighThresholdReset(uiState.selectedVehicleClass) },
                        resetContentDescription = stringResource(Res.string.tyre_temperature_high_threshold_reset),
                    )
                }
            },
        )
        DetailPaneCard(
            title = stringResource(Res.string.tyre_temperature_low_warning_card_title),
            checked = uiState.lowWarningEnabled,
            onCheckedChange = onLowWarningEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.tyre_temperature_cold_text_label),
                        savedText = uiState.coldReadoutText,
                        defaultText = LMU_WINDOWS_TYRE_TEMPERATURE_COLD_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onColdReadoutTextChanged,
                        onPreviewClick = onLowWarningPreviewClicked,
                        placeholder = LMU_WINDOWS_TYRE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.tyre_temperature_cold_celsius_insert),
                        placeholderHint = stringResource(Res.string.tyre_temperature_cold_celsius_placeholder_hint),
                        resetContentDescription = stringResource(Res.string.tyre_temperature_cold_text_reset),
                        previewContentDescription = stringResource(Res.string.tyre_temperature_cold_text_preview),
                        selectedContentDescription =
                            stringResource(Res.string.tyre_temperature_cold_text_selected_icon),
                        unavailableText = stringResource(Res.string.tyre_temperature_cold_text_unavailable),
                        supportingText = stringResource(Res.string.tyre_temperature_cold_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownLmuWindowsTyreTemperatureReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let { stringResource(Res.string.tyre_temperature_cold_text_unknown_placeholders, it) }
                        },
                    )
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.tyre_temperature_low_warning_phases_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = lowWarningPhasesHelpIconContentDescription,
                                sheetContent = { TyreTemperatureLowWarningPhasesHelpSheetContent() },
                            )
                        },
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.small),
                        modifier =
                            Modifier.fillMaxWidth().padding(
                                horizontal = KoDriverSpacing.extraSmall,
                                vertical = KoDriverSpacing.extraSmall,
                            ),
                    ) {
                        phaseLabels.forEach { (phase, label) ->
                            val selected = phase in uiState.lowWarningPhases
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.ContextClick)
                                    onLowWarningPhaseToggled(phase)
                                },
                                label = { Text(text = label) },
                                leadingIcon =
                                    if (selected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                            )
                                        }
                                    } else {
                                        null
                                    },
                            )
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun VehicleClassImageOption(
    image: org.jetbrains.compose.resources.DrawableResource,
    accessibleName: String,
    celsius: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (selected) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant),
        modifier =
            modifier.semantics { contentDescription = accessibleName }.selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            ),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            modifier = Modifier.padding(KoDriverSpacing.extraSmall),
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(VEHICLE_CLASS_IMAGE_SIZE),
            )
            Text("$celsius°C", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private val VEHICLE_CLASS_OPTION_WIDTH = 104.dp
private val VEHICLE_CLASS_IMAGE_SIZE = 72.dp

@Composable
internal fun TyreTemperatureThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Composable
internal fun TyreTemperatureLowWarningPhasesHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.tyre_temperature_low_warning_phases_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsAutoReadoutTyreTemperatureDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsAutoReadoutTyreTemperatureDetailPaneContent(
            uiState = LmuWindowsAutoReadoutTyreTemperatureDetailUiState(),
        )
    }
}
