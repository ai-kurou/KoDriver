package kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail

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
import androidx.compose.material3.HorizontalDivider
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
import kurou.kodriver.core.designsystem.DetailPaneBodyText
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
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MIN
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsBrakeWearReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_description
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_percent_insert
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_percent_placeholder_hint
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_label
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_preview
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_selected_icon
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_supporting
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_unavailable
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_threshold_description
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_threshold_help_description
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_threshold_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_threshold_label
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_threshold_reset
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_vehicle_class_target_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.brakeweardetail.generated.resources.brake_wear_warning_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kurou.kodriver.core.designsystem.generated.resources.Res as DesignSystemRes

private const val THRESHOLD_MIN = LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MIN.toFloat()
private const val THRESHOLD_MAX = LMU_WINDOWS_BRAKE_WEAR_LOW_THRESHOLD_PERCENT_MAX.toFloat()

/**
 * LmuWindowsAutoReadoutBrakeWearDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsAutoReadoutBrakeWearDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsAutoReadoutBrakeWearDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onVehicleClassSelected = viewModel::onVehicleClassSelected,
        onThresholdChanged = viewModel::onVehicleClassLowThresholdChanged,
        onThresholdReset = viewModel::onVehicleClassLowThresholdReset,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsAutoReadoutBrakeWearDetailPaneContent(
    uiState: LmuWindowsAutoReadoutBrakeWearDetailUiState = LmuWindowsAutoReadoutBrakeWearDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    onVehicleClassSelected: (LmuWindowsVehicleClassData) -> Unit = {},
    onThresholdChanged: (LmuWindowsVehicleClassData, Int) -> Unit = { _, _ -> },
    onThresholdReset: (LmuWindowsVehicleClassData) -> Unit = {},
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
            text = stringResource(Res.string.brake_wear_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.brake_wear_threshold_label)
        val helpIconContentDescription =
            stringResource(Res.string.brake_wear_threshold_help_icon_content_description)
        DetailPaneCard(
            title = stringResource(Res.string.brake_wear_warning_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.brake_wear_text_label),
                        savedText = uiState.readoutText,
                        defaultText = LMU_WINDOWS_BRAKE_WEAR_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = onReadoutTextPreviewClicked,
                        placeholder = LMU_WINDOWS_BRAKE_WEAR_PERCENT_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.brake_wear_percent_insert),
                        placeholderHint = stringResource(Res.string.brake_wear_percent_placeholder_hint),
                        resetContentDescription = stringResource(Res.string.brake_wear_text_reset_to_default),
                        previewContentDescription = stringResource(Res.string.brake_wear_text_preview),
                        selectedContentDescription = stringResource(Res.string.brake_wear_text_selected_icon),
                        unavailableText = stringResource(Res.string.brake_wear_text_unavailable),
                        supportingText = stringResource(Res.string.brake_wear_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownLmuWindowsBrakeWearReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let { stringResource(Res.string.brake_wear_text_unknown_placeholders, it) }
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
                        text = stringResource(Res.string.brake_wear_vehicle_class_target_subtitle),
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
                            val percent = uiState.vehicleClassLowThresholdPercent[vehicleClass]
                            if (percent != null) {
                                VehicleClassImageOption(
                                    image = image,
                                    accessibleName = "$accessibleName、$percent%",
                                    percent = percent,
                                    selected = uiState.selectedVehicleClass == vehicleClass,
                                    enabled = uiState.enabled,
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
                        text = stringResource(Res.string.brake_wear_threshold_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = helpIconContentDescription,
                                sheetContent = { BrakeWearThresholdHelpSheetContent() },
                            )
                        },
                    )
                    val selectedVehicleClassLowThresholdPercent =
                        uiState.vehicleClassLowThresholdPercent[uiState.selectedVehicleClass]
                            ?: lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
                                uiState.selectedVehicleClass,
                            )
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.brake_wear_threshold_description)
                                .formatSliderLabel(selectedVehicleClassLowThresholdPercent),
                    )
                    ThresholdSlider(
                        value = selectedVehicleClassLowThresholdPercent.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            onThresholdChanged(uiState.selectedVehicleClass, it.roundToInt())
                        },
                        defaultValue =
                            lmuWindowsVehicleClassBrakeWearLowThresholdPercentDefault(
                                uiState.selectedVehicleClass,
                            ).toFloat(),
                        onResetToDefault = { onThresholdReset(uiState.selectedVehicleClass) },
                        resetContentDescription = stringResource(Res.string.brake_wear_threshold_reset),
                    )
                }
            },
        )
    }
}

@Composable
private fun VehicleClassImageOption(
    image: org.jetbrains.compose.resources.DrawableResource,
    accessibleName: String,
    percent: Int,
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
            Text("$percent%", style = MaterialTheme.typography.labelLarge)
        }
    }
}

private val VEHICLE_CLASS_OPTION_WIDTH = 104.dp
private val VEHICLE_CLASS_IMAGE_SIZE = 72.dp

@Composable
internal fun BrakeWearThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.brake_wear_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsAutoReadoutBrakeWearDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsAutoReadoutBrakeWearDetailPaneContent()
    }
}
