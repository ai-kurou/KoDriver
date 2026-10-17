package kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardTextField
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.HelpIconButton
import kurou.kodriver.core.designsystem.KoDriverHelpSheetImageContent
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.KoDriverTheme
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.core.designsystem.formatSliderLabel
import kurou.kodriver.core.designsystem.rememberPendingText
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT
import kurou.kodriver.domain.acewindows.model.ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT
import kurou.kodriver.domain.model.READOUT_CUSTOM_TEXT_MAX_LENGTH
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_chip_label
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_description
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_description
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_help_icon_content_description
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_start_readout_switch_label
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_preview
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_reset_to_default
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_selected_icon
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_supporting
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_text_unavailable
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_label
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_reset_to_default
import kurou.kodriver.feature.acewindowsautoreadout.vehicleapproachdetail.generated.resources.vehicle_approach_threshold_subtitle
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * AceWindowsAutoReadoutVehicleApproachDetail の画面を表示する Composable。
 */
@Composable
fun AceWindowsAutoReadoutVehicleApproachDetailPane(modifier: Modifier = Modifier) {
    val viewModel: AceWindowsAutoReadoutVehicleApproachDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DisposableEffect(viewModel) {
        onDispose { viewModel.onPreviewStopped() }
    }
    AceWindowsAutoReadoutVehicleApproachDetailPaneContent(
        uiState = uiState,
        onThresholdChanged = viewModel::onThresholdChanged,
        onResetThreshold = viewModel::onResetThreshold,
        onStartReadoutEnabledChanged = viewModel::onStartReadoutEnabledChanged,
        onReadoutTextChanged = viewModel::onReadoutTextChanged,
        onReadoutTextReset = viewModel::onReadoutTextReset,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AceWindowsAutoReadoutVehicleApproachDetailPaneContent(
    uiState: AceWindowsAutoReadoutVehicleApproachDetailUiState = AceWindowsAutoReadoutVehicleApproachDetailUiState(),
    onThresholdChanged: (Double) -> Unit = {},
    onResetThreshold: () -> Unit = {},
    onStartReadoutEnabledChanged: (Boolean) -> Unit = {},
    onReadoutTextChanged: (String) -> Unit = {},
    onReadoutTextReset: () -> Unit = {},
    onPreviewClicked: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val thresholdLabel = stringResource(Res.string.vehicle_approach_threshold_label)

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
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
        val resetToDefaultLabel = stringResource(Res.string.vehicle_approach_threshold_reset_to_default)
        ThresholdSlider(
            value = uiState.thresholdMeters.toFloat(),
            valueRange = 2f..10f,
            labelFormatter = { thresholdLabel.formatSliderLabel(it) },
            onValueChangeFinished = { onThresholdChanged(it.toDouble()) },
            modifier = Modifier.padding(horizontal = KoDriverSpacing.large),
            defaultValue = ACE_WINDOWS_VEHICLE_APPROACH_THRESHOLD_METERS_DEFAULT.toFloat(),
            onResetToDefault = onResetThreshold,
            resetContentDescription = resetToDefaultLabel,
        )
        val textState = rememberPendingText(uiState.readoutText, READOUT_CUSTOM_TEXT_MAX_LENGTH)
        val currentText = textState.currentText
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_approach_start_readout_switch_label),
            checked = uiState.startReadoutEnabled,
            onCheckedChange = onStartReadoutEnabledChanged,
            modifier = Modifier.padding(horizontal = KoDriverSpacing.small, vertical = KoDriverSpacing.extraSmall),
            bottomContent = {
                DetailPaneCardTextField(
                    value = currentText,
                    defaultValue = ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT,
                    onResetToDefault = {
                        textState.change(ACE_WINDOWS_VEHICLE_APPROACH_READOUT_TEXT_DEFAULT)
                        onReadoutTextReset()
                    },
                    resetContentDescription = stringResource(Res.string.vehicle_approach_text_reset_to_default),
                    placeholder = stringResource(Res.string.vehicle_approach_chip_label),
                    maxLength = READOUT_CUSTOM_TEXT_MAX_LENGTH,
                    onValueChangeFinished = {
                        textState.change(it)
                        onReadoutTextChanged(it)
                    },
                    onPreviewClick = onPreviewClicked,
                    enabled = uiState.isTextToSpeechAvailable,
                    selected = currentText.isNotBlank(),
                    supportingText =
                        when {
                            !uiState.isTextToSpeechAvailable -> {
                                stringResource(Res.string.vehicle_approach_text_unavailable)
                            }

                            currentText.isNotBlank() -> {
                                null
                            }

                            else -> {
                                stringResource(Res.string.vehicle_approach_text_supporting)
                            }
                        },
                    previewContentDescription = stringResource(Res.string.vehicle_approach_text_preview),
                    selectedContentDescription = stringResource(Res.string.vehicle_approach_text_selected_icon),
                )
            },
        )
    }
}

@Composable
internal fun VehicleApproachHelpSheetContent(modifier: Modifier = Modifier) {
    KoDriverHelpSheetImageContent(
        description = stringResource(Res.string.vehicle_approach_help_description),
        painter = painterResource(Res.drawable.vehicle_approach),
        modifier = modifier,
    )
}

@Preview(showBackground = true)
@Composable
private fun AceWindowsAutoReadoutVehicleApproachDetailPanePreview() {
    KoDriverTheme {
        AceWindowsAutoReadoutVehicleApproachDetailPaneContent(
            uiState = AceWindowsAutoReadoutVehicleApproachDetailUiState(isTextToSpeechAvailable = true),
        )
    }
}
