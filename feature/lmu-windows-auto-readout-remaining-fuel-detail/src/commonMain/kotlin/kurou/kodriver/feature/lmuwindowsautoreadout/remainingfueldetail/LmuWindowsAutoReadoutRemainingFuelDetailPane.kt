package kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneBodyText
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
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
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MAX
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MIN
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsEnergySource
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVehicleClassData
import kurou.kodriver.domain.lmuwindows.model.findUnknownLmuWindowsRemainingFuelReadoutPlaceholders
import kurou.kodriver.domain.lmuwindows.model.lmuWindowsVehicleClassEnergySource
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_description
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_percent_insert
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_percent_placeholder_hint
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_label
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_preview
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_selected_icon
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_supporting
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_unavailable
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_text_unknown_placeholders
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_description
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_label
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_reset
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_threshold_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_vehicle_class_not_target
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_vehicle_class_target
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_vehicle_class_target_subtitle
import kurou.kodriver.feature.lmuwindowsautoreadout.remainingfueldetail.generated.resources.remaining_fuel_warning_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kurou.kodriver.core.designsystem.generated.resources.Res as DesignSystemRes

private const val THRESHOLD_MIN = LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MIN.toFloat()
private const val THRESHOLD_MAX = LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_MAX.toFloat()

/**
 * LmuWindowsAutoReadoutRemainingFuelDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsAutoReadoutRemainingFuelDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsAutoReadoutRemainingFuelDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
        uiState = uiState,
        onEnabledChanged = viewModel::onEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextPreviewClicked = viewModel::onReadoutTextPreviewClicked,
        onThresholdChanged = viewModel::onThresholdChanged,
        onThresholdReset = viewModel::onThresholdReset,
        modifier = modifier,
    )
}

@Suppress("LongParameterList")
@Composable
internal fun LmuWindowsAutoReadoutRemainingFuelDetailPaneContent(
    uiState: LmuWindowsAutoReadoutRemainingFuelDetailUiState =
        LmuWindowsAutoReadoutRemainingFuelDetailUiState(),
    onEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextPreviewClicked: (String) -> Unit = {},
    onThresholdChanged: (Int) -> Unit = {},
    onThresholdReset: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.remaining_fuel_description),
        )
        val thresholdLabelTemplate = stringResource(Res.string.remaining_fuel_threshold_label)
        DetailPaneCard(
            title = stringResource(Res.string.remaining_fuel_warning_title),
            checked = uiState.enabled,
            onCheckedChange = onEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.remaining_fuel_vehicle_class_target_subtitle),
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
                            EnergySourceVehicleClassOption(
                                image = image,
                                accessibleName = accessibleName,
                                target =
                                    lmuWindowsVehicleClassEnergySource(vehicleClass) == LmuWindowsEnergySource.Fuel,
                                modifier = Modifier.width(VEHICLE_CLASS_OPTION_WIDTH),
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.small,
                            ),
                    )
                    PlaceholderReadoutTextField(
                        label = stringResource(Res.string.remaining_fuel_text_label),
                        savedText = uiState.readoutText,
                        defaultText = LMU_WINDOWS_REMAINING_FUEL_READOUT_TEXT_DEFAULT,
                        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        onTextChanged = onReadoutTextChanged,
                        onPreviewClick = onReadoutTextPreviewClicked,
                        placeholder = LMU_WINDOWS_REMAINING_FUEL_PERCENT_PLACEHOLDER,
                        insertLabel = stringResource(Res.string.remaining_fuel_percent_insert),
                        placeholderHint = stringResource(Res.string.remaining_fuel_percent_placeholder_hint),
                        resetContentDescription =
                            stringResource(Res.string.remaining_fuel_text_reset_to_default),
                        previewContentDescription = stringResource(Res.string.remaining_fuel_text_preview),
                        selectedContentDescription =
                            stringResource(Res.string.remaining_fuel_text_selected_icon),
                        unavailableText = stringResource(Res.string.remaining_fuel_text_unavailable),
                        supportingText = stringResource(Res.string.remaining_fuel_text_supporting),
                        unknownPlaceholdersText = { currentText ->
                            findUnknownLmuWindowsRemainingFuelReadoutPlaceholders(currentText)
                                .joinToString("、")
                                .takeIf { it.isNotEmpty() }
                                ?.let {
                                    stringResource(Res.string.remaining_fuel_text_unknown_placeholders, it)
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
                    DetailPaneSubtitle(text = stringResource(Res.string.remaining_fuel_threshold_subtitle))
                    DetailPaneBodyText(
                        text =
                            stringResource(Res.string.remaining_fuel_threshold_description)
                                .formatSliderLabel(uiState.thresholdPercentage),
                    )
                    ThresholdSlider(
                        value = uiState.thresholdPercentage.toFloat(),
                        valueRange = THRESHOLD_MIN..THRESHOLD_MAX,
                        steps = (THRESHOLD_MAX - THRESHOLD_MIN).toInt() - 1,
                        labelFormatter = { thresholdLabelTemplate.formatSliderLabel(it.roundToInt()) },
                        onValueChangeFinished = { onThresholdChanged(it.roundToInt()) },
                        defaultValue = LMU_WINDOWS_REMAINING_FUEL_THRESHOLD_PERCENTAGE_DEFAULT.toFloat(),
                        onResetToDefault = onThresholdReset,
                        resetContentDescription = stringResource(Res.string.remaining_fuel_threshold_reset),
                    )
                }
            },
        )
    }
}

@Composable
private fun EnergySourceVehicleClassOption(
    image: org.jetbrains.compose.resources.DrawableResource,
    accessibleName: String,
    target: Boolean,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val targetLabel =
        stringResource(
            if (target) {
                Res.string.remaining_fuel_vehicle_class_target
            } else {
                Res.string.remaining_fuel_vehicle_class_not_target
            },
        )
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = if (target) colors.primaryContainer else colors.surfaceContainerLow,
        contentColor = if (target) colors.onPrimaryContainer else colors.onSurface,
        border = BorderStroke(1.dp, if (target) colors.primary else colors.outlineVariant),
        modifier =
            modifier.semantics(mergeDescendants = true) {
                contentDescription = "$accessibleName、$targetLabel"
            },
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall),
            modifier = Modifier.padding(KoDriverSpacing.extraSmall).alpha(if (target) 1f else 0.5f),
        ) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                modifier = Modifier.size(VEHICLE_CLASS_IMAGE_SIZE),
            )
            Text(targetLabel, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private val VEHICLE_CLASS_OPTION_WIDTH = 104.dp
private val VEHICLE_CLASS_IMAGE_SIZE = 72.dp

@Preview(showBackground = true)
@Composable
private fun LmuWindowsAutoReadoutRemainingFuelDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsAutoReadoutRemainingFuelDetailPaneContent()
    }
}
