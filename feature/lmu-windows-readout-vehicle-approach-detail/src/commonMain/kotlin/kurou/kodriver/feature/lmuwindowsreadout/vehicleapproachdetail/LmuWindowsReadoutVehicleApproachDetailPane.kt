package kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneLabeledTextField
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_first_lap_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_description
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_icon_content_description
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_lateral_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_longitudinal_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_skip_first_lap_subtitle
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_skip_first_lap_switch_content_description
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_left_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_readout_switch_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_right_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_sustained_duration_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_sustained_readout_switch_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_sustained_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_preview
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_selected_icon
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_supporting
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_unavailable
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_reset_to_default
import kurou.kodriver.feature.lmuwindowsreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_subtitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

/**
 * LmuWindowsReadoutVehicleApproachDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutVehicleApproachDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutVehicleApproachDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutVehicleApproachDetailPaneContent(
        uiState = uiState,
        onLongitudinalThresholdChanged = viewModel::onLongitudinalThresholdChanged,
        onLateralThresholdChanged = viewModel::onLateralThresholdChanged,
        onResetLongitudinalThreshold = viewModel::onResetLongitudinalThreshold,
        onResetLateralThreshold = viewModel::onResetLateralThreshold,
        onSustainedApproachDurationSecondsChanged = viewModel::onSustainedApproachDurationSecondsChanged,
        onResetSustainedApproachDurationSeconds = viewModel::onResetSustainedApproachDurationSeconds,
        onSkipFirstLapChanged = viewModel::onSkipFirstLapChanged,
        onStartReadoutEnabledChanged = viewModel::onStartReadoutEnabledChanged,
        onSustainedReadoutEnabledChanged = viewModel::onSustainedReadoutEnabledChanged,
        onStartLeftTextChanged = viewModel::onStartLeftTextChanged,
        onStartLeftTextReset = viewModel::onStartLeftTextReset,
        onStartRightTextChanged = viewModel::onStartRightTextChanged,
        onStartRightTextReset = viewModel::onStartRightTextReset,
        onStartLeftTextPreviewClicked = viewModel::onStartLeftTextPreviewClicked,
        onStartRightTextPreviewClicked = viewModel::onStartRightTextPreviewClicked,
        onSustainedLeftTextChanged = viewModel::onSustainedLeftTextChanged,
        onSustainedLeftTextReset = viewModel::onSustainedLeftTextReset,
        onSustainedRightTextChanged = viewModel::onSustainedRightTextChanged,
        onSustainedRightTextReset = viewModel::onSustainedRightTextReset,
        onSustainedLeftTextPreviewClicked = viewModel::onSustainedLeftTextPreviewClicked,
        onSustainedRightTextPreviewClicked = viewModel::onSustainedRightTextPreviewClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsReadoutVehicleApproachDetailPaneContent(
    uiState: LmuWindowsReadoutVehicleApproachDetailUiState,
    modifier: Modifier = Modifier,
    onLongitudinalThresholdChanged: (Double) -> Unit = {},
    onLateralThresholdChanged: (Double) -> Unit = {},
    onResetLongitudinalThreshold: () -> Unit = {},
    onResetLateralThreshold: () -> Unit = {},
    onSustainedApproachDurationSecondsChanged: (Int) -> Unit = {},
    onResetSustainedApproachDurationSeconds: () -> Unit = {},
    onSkipFirstLapChanged: (Boolean) -> Unit = {},
    onStartReadoutEnabledChanged: (Boolean) -> Unit = {},
    onSustainedReadoutEnabledChanged: (Boolean) -> Unit = {},
    onStartLeftTextChanged: (String) -> Unit = {},
    onStartLeftTextReset: () -> Unit = {},
    onStartRightTextChanged: (String) -> Unit = {},
    onStartRightTextReset: () -> Unit = {},
    onStartLeftTextPreviewClicked: (String) -> Unit = {},
    onStartRightTextPreviewClicked: (String) -> Unit = {},
    onSustainedLeftTextChanged: (String) -> Unit = {},
    onSustainedLeftTextReset: () -> Unit = {},
    onSustainedRightTextChanged: (String) -> Unit = {},
    onSustainedRightTextReset: () -> Unit = {},
    onSustainedLeftTextPreviewClicked: (String) -> Unit = {},
    onSustainedRightTextPreviewClicked: (String) -> Unit = {},
) {
    val longitudinalLabel = stringResource(Res.string.vehicle_approach_longitudinal_label)
    val lateralLabel = stringResource(Res.string.vehicle_approach_lateral_label)
    val sustainedDurationLabel = stringResource(Res.string.vehicle_approach_sustained_duration_label)

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DetailPaneDescription(
            text = stringResource(Res.string.vehicle_approach_description),
        )
        DetailPaneSubtitle(
            text = stringResource(Res.string.vehicle_approach_threshold_subtitle),
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            trailingContent = {
                HelpIconButton(
                    contentDescription = stringResource(Res.string.vehicle_approach_help_icon_content_description),
                    sheetContent = { VehicleApproachHelpSheetContent() },
                )
            },
        )
        val defaultLongitudinal =
            LMU_WINDOWS_VEHICLE_APPROACH_LONGITUDINAL_THRESHOLD_METERS_DEFAULT.toFloat()
        val defaultLateral = LMU_WINDOWS_VEHICLE_APPROACH_LATERAL_THRESHOLD_METERS_DEFAULT.toFloat()
        val defaultSustainedDuration =
            LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_DURATION_SECONDS_DEFAULT.toFloat()
        val resetToDefaultLabel = stringResource(Res.string.vehicle_approach_threshold_reset_to_default)
        ThresholdSlider(
            value = uiState.longitudinalThresholdMeters.toFloat(),
            valueRange = 0.1f..10f,
            labelFormatter = { longitudinalLabel.formatSliderLabel(it) },
            onValueChangeFinished = { onLongitudinalThresholdChanged(it.toDouble()) },
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            defaultValue = defaultLongitudinal,
            onResetToDefault = onResetLongitudinalThreshold,
            resetContentDescription = resetToDefaultLabel,
        )
        ThresholdSlider(
            value = uiState.lateralThresholdMeters.toFloat(),
            valueRange = 2f..8f,
            labelFormatter = { lateralLabel.formatSliderLabel(it) },
            onValueChangeFinished = { onLateralThresholdChanged(it.toDouble()) },
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            defaultValue = defaultLateral,
            onResetToDefault = onResetLateralThreshold,
            resetContentDescription = resetToDefaultLabel,
        )
        DetailPaneSubtitle(
            text = stringResource(Res.string.vehicle_approach_first_lap_subtitle),
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier.fillMaxWidth().padding(
                    horizontal = KoDriverSpacing.large,
                    vertical = KoDriverSpacing.extraSmall,
                ),
        ) {
            Text(
                text = stringResource(Res.string.vehicle_approach_skip_first_lap_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(KoDriverSpacing.large))
            val skipFirstLapSwitchDescription =
                stringResource(Res.string.vehicle_approach_skip_first_lap_switch_content_description)
            Switch(
                checked = uiState.skipFirstLap,
                onCheckedChange = onSkipFirstLapChanged,
                modifier = Modifier.semantics { contentDescription = skipFirstLapSwitchDescription },
            )
        }
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_approach_start_readout_switch_label),
            checked = uiState.startReadoutEnabled,
            onCheckedChange = onStartReadoutEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.large)) {
                    ReadoutTextField(
                        label = stringResource(Res.string.vehicle_approach_start_left_label),
                        text = uiState.startLeftText,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        unavailableText = stringResource(Res.string.vehicle_approach_text_unavailable),
                        onTextChanged = onStartLeftTextChanged,
                        defaultText = LMU_WINDOWS_VEHICLE_APPROACH_START_LEFT_READOUT_TEXT_DEFAULT,
                        onReset = onStartLeftTextReset,
                        onPreviewClick = onStartLeftTextPreviewClicked,
                    )
                    ReadoutTextField(
                        label = stringResource(Res.string.vehicle_approach_start_right_label),
                        text = uiState.startRightText,
                        isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                        unavailableText = stringResource(Res.string.vehicle_approach_text_unavailable),
                        onTextChanged = onStartRightTextChanged,
                        defaultText = LMU_WINDOWS_VEHICLE_APPROACH_START_RIGHT_READOUT_TEXT_DEFAULT,
                        onReset = onStartRightTextReset,
                        onPreviewClick = onStartRightTextPreviewClicked,
                    )
                }
            },
        )
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_approach_sustained_readout_switch_label),
            checked = uiState.sustainedReadoutEnabled,
            onCheckedChange = onSustainedReadoutEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.large)) {
                        ReadoutTextField(
                            label = stringResource(Res.string.vehicle_approach_start_left_label),
                            text = uiState.sustainedLeftText,
                            isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                            unavailableText = stringResource(Res.string.vehicle_approach_sustained_text_unavailable),
                            onTextChanged = onSustainedLeftTextChanged,
                            defaultText = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_LEFT_READOUT_TEXT_DEFAULT,
                            onReset = onSustainedLeftTextReset,
                            onPreviewClick = onSustainedLeftTextPreviewClicked,
                        )
                        ReadoutTextField(
                            label = stringResource(Res.string.vehicle_approach_start_right_label),
                            text = uiState.sustainedRightText,
                            isTextToSpeechAvailable = uiState.isTextToSpeechAvailable,
                            unavailableText = stringResource(Res.string.vehicle_approach_sustained_text_unavailable),
                            onTextChanged = onSustainedRightTextChanged,
                            defaultText = LMU_WINDOWS_VEHICLE_APPROACH_SUSTAINED_RIGHT_READOUT_TEXT_DEFAULT,
                            onReset = onSustainedRightTextReset,
                            onPreviewClick = onSustainedRightTextPreviewClicked,
                        )
                    }
                    HorizontalDivider(
                        modifier =
                            Modifier.padding(
                                horizontal = KoDriverSpacing.small,
                                vertical = KoDriverSpacing.medium,
                            ),
                    )
                    ThresholdSlider(
                        value = uiState.sustainedApproachDurationSeconds.toFloat(),
                        valueRange = 4f..10f,
                        steps = 5,
                        labelFormatter = { sustainedDurationLabel.formatSliderLabel(it.roundToInt().toFloat()) },
                        onValueChangeFinished = { onSustainedApproachDurationSecondsChanged(it.roundToInt()) },
                        defaultValue = defaultSustainedDuration,
                        onResetToDefault = onResetSustainedApproachDurationSeconds,
                        resetContentDescription = resetToDefaultLabel,
                    )
                }
            },
        )
    }
}

/** 左右それぞれの読み上げ文言の入力と試聴を提供する。空白の場合は読み上げない。 */
@Composable
private fun ReadoutTextField(
    label: String,
    text: String,
    defaultText: String,
    onReset: () -> Unit,
    isTextToSpeechAvailable: Boolean,
    unavailableText: String,
    onTextChanged: (String) -> Unit,
    onPreviewClick: (String) -> Unit,
) {
    var currentText by remember { mutableStateOf(text) }
    // 古い保存結果で入力を巻き戻さず、正規化後の保存値が一致したら待機を解除する。
    var pendingText by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(text, pendingText) {
        if (pendingText == null) {
            currentText = text
        } else if (pendingText == text) {
            pendingText = null
        }
    }
    val changeText: (String) -> Unit = {
        currentText = it
        pendingText = it.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
        onTextChanged(it)
    }
    DetailPaneLabeledTextField(
        label = label,
        value = currentText,
        defaultValue = defaultText,
        onResetToDefault = {
            currentText = defaultText
            pendingText = defaultText.trim().take(READOUT_CUSTOM_TEXT_MAX_LENGTH)
            onReset()
        },
        resetContentDescription = stringResource(Res.string.vehicle_approach_text_reset_to_default),
        maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
        onValueChangeFinished = changeText,
        onPreviewClick = onPreviewClick,
        enabled = isTextToSpeechAvailable,
        selected = currentText.isNotBlank(),
        supportingText =
            when {
                !isTextToSpeechAvailable -> unavailableText
                currentText.isNotBlank() -> null
                else -> stringResource(Res.string.vehicle_approach_text_supporting)
            },
        previewContentDescription = stringResource(Res.string.vehicle_approach_text_preview),
        selectedContentDescription = stringResource(Res.string.vehicle_approach_text_selected_icon),
    )
}

@Composable
internal fun VehicleApproachHelpSheetContent(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = KoDriverSpacing.large)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = stringResource(Res.string.vehicle_approach_help_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Image(
                painter = painterResource(Res.drawable.vehicle_approach),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth(0.3f).padding(start = KoDriverSpacing.large),
            )
        }
        Spacer(modifier = Modifier.height(KoDriverSpacing.extraLarge))
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutVehicleApproachDetailPanePreview() {
    KoDriverTheme {
        LmuWindowsReadoutVehicleApproachDetailPaneContent(uiState = LmuWindowsReadoutVehicleApproachDetailUiState())
    }
}
