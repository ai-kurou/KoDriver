package kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail

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
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_celsius_insert
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_celsius_placeholder_hint
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_description
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_label
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_preview
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_selected_icon
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_supporting
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_unavailable
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_description
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_help_description
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_label
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_reset
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_vehicle_class_target_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.braketemperaturedetail.generated.resources.brake_temperature_warning_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kurou.kodriver.core.designsystem.generated.resources.Res as DesignSystemRes

private const val THRESHOLD_MIN = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MIN.toFloat()
private const val THRESHOLD_MAX = LMU_WINDOWS_BRAKE_TEMPERATURE_HIGH_THRESHOLD_CELSIUS_MAX.toFloat()

/**
 * LmuWindowsAutoReadoutBrakeTemperatureDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsAutoReadoutBrakeTemperatureDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsAutoReadoutBrakeTemperatureDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onVehicleClassSelected = viewModel::onVehicleClassSelected,
        onThresholdChanged = viewModel::onVehicleClassHighThresholdChanged,
        onThresholdReset = viewModel::onVehicleClassHighThresholdReset,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent(
    uiState: LmuWindowsAutoReadoutBrakeTemperatureDetailUiState = LmuWindowsAutoReadoutBrakeTemperatureDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    onVehicleClassSelected: (LmuWindowsVehicleClassData) -> Unit = {},
    onThresholdChanged: (LmuWindowsVehicleClassData, Int) -> Unit = { _, _ -> },
    onThresholdReset: (LmuWindowsVehicleClassData) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.brake_temperature_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.brake_temperature_threshold_label)
        val helpIconContentDescription =
            stringResource(Res.string.brake_temperature_threshold_help_icon_content_description)
        DetailPaneCard(
            title = stringResource(Res.string.brake_temperature_warning_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.brake_temperature_text_label),
                        savedText = uiState.readoutText,
                        defaultText = LMU_WINDOWS_BRAKE_TEMPERATURE_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = onReadoutTextPreviewClicked,
                        placeholder = LMU_WINDOWS_BRAKE_TEMPERATURE_CELSIUS_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.brake_temperature_celsius_insert),
                        placeholderHint = stringResource(Res.string.brake_temperature_celsius_placeholder_hint),
                        resetContentDescription = stringResource(Res.string.brake_temperature_text_reset_to_default),
                        previewContentDescription = stringResource(Res.string.brake_temperature_text_preview),
                        selectedContentDescription = stringResource(Res.string.brake_temperature_text_selected_icon),
                        unavailableText = stringResource(Res.string.brake_temperature_text_unavailable),
                        supportingText = stringResource(Res.string.brake_temperature_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownLmuWindowsBrakeTemperatureReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let { stringResource(Res.string.brake_temperature_text_unknown_placeholders, it) }
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
                        text = stringResource(Res.string.brake_temperature_vehicle_class_target_subtitle),
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
                                    enabled = uiState.enabled,
                                    onClick = { onVehicleClassSelected(vehicleClass) },
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
                        text = stringResource(Res.string.brake_temperature_threshold_subtitle),
                        trailingContent = {
                            HelpIconButton(
                                contentDescription = helpIconContentDescription,
                                sheetContent = { BrakeTemperatureThresholdHelpSheetContent() },
                            )
                        },
                    )
                    val selectedVehicleClassHighThresholdCelsius =
                        uiState.vehicleClassHighThresholdCelsius[uiState.selectedVehicleClass]
                            ?: lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            )
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.brake_temperature_threshold_description)
                                .formatSliderLabel(selectedVehicleClassHighThresholdCelsius),
                    )
                    ThresholdSlider(
                        value = selectedVehicleClassHighThresholdCelsius.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = {
                            onThresholdChanged(uiState.selectedVehicleClass, it.roundToInt())
                        },
                        defaultValue =
                            lmuWindowsVehicleClassBrakeTemperatureHighThresholdCelsiusDefault(
                                uiState.selectedVehicleClass,
                            ).toFloat(),
                        onResetToDefault = { onThresholdReset(uiState.selectedVehicleClass) },
                        resetContentDescription = stringResource(Res.string.brake_temperature_threshold_reset),
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
internal fun BrakeTemperatureThresholdHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.brake_temperature_threshold_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large),
    )
    Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsAutoReadoutBrakeTemperatureDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsAutoReadoutBrakeTemperatureDetailPaneContent()
    }
}
