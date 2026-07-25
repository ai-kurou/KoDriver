package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_description
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_help_description
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_reset_to_default
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_laps_slider_label
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_tyre_wear_laps_help_icon_content_description
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_tyre_wear_laps_subtitle
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_laps_help_icon_content_description
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_laps_subtitle
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_title
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_voice_type
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.ThresholdSlider
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT
import kurou.kodriver.domain.model.LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt

private const val MINIMUM_PIT_TIMING_LAPS = 1f
private const val MAXIMUM_PIT_TIMING_LAPS = 5f

@Composable
fun LmuWindowsReadoutPitTimingDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutPitTimingDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutPitTimingDetailPaneContent(
        uiState = uiState,
        onVirtualEnergyLapsChanged = viewModel::onVirtualEnergyLapsChanged,
        onTyreWearLapsChanged = viewModel::onTyreWearLapsChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LmuWindowsReadoutPitTimingDetailPaneContent(
    uiState: LmuWindowsReadoutPitTimingDetailUiState = LmuWindowsReadoutPitTimingDetailUiState(),
    onVirtualEnergyLapsChanged: (Int) -> Unit = {},
    onTyreWearLapsChanged: (Int) -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val sliderLabel = stringResource(Res.string.pit_timing_laps_slider_label)
    val resetToDefaultLabel = stringResource(Res.string.pit_timing_laps_reset_to_default)
    val voiceTypeLabel = stringResource(Res.string.pit_timing_voice_type)
    val virtualEnergyHelpIconContentDescription =
        stringResource(Res.string.pit_timing_virtual_energy_laps_help_icon_content_description)
    val tyreWearHelpIconContentDescription =
        stringResource(Res.string.pit_timing_tyre_wear_laps_help_icon_content_description)

    var showLapsHelpSheet by remember { mutableStateOf(false) }
    val lapsHelpSheetState = rememberModalBottomSheetState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.pit_timing_description),
        )
        DetailPaneCard(
            title = stringResource(Res.string.pit_timing_virtual_energy_title),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailPaneCardChips(
                        chipLabels = listOf(voiceTypeLabel),
                        selectedChipLabels = setOf(voiceTypeLabel),
                        chipEnabled = true,
                        onChipClick = { onPreviewClicked() },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp))
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.pit_timing_virtual_energy_laps_subtitle),
                        trailingContent = {
                            IconButton(onClick = { showLapsHelpSheet = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = virtualEnergyHelpIconContentDescription,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        },
                    )
                    ThresholdSlider(
                        value = uiState.virtualEnergyLaps.toFloat(),
                        valueRange = MINIMUM_PIT_TIMING_LAPS..MAXIMUM_PIT_TIMING_LAPS,
                        labelFormatter = { sliderLabel.format(it.roundToInt()) },
                        onValueChangeFinished = { onVirtualEnergyLapsChanged(it.roundToInt()) },
                        steps = 3,
                        defaultValue = LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT.toFloat(),
                        onResetToDefault = {
                            onVirtualEnergyLapsChanged(LMU_WINDOWS_PIT_TIMING_VIRTUAL_ENERGY_LAPS_DEFAULT)
                        },
                        resetContentDescription = resetToDefaultLabel,
                    )
                    DetailPaneSubtitle(
                        text = stringResource(Res.string.pit_timing_tyre_wear_laps_subtitle),
                        trailingContent = {
                            IconButton(onClick = { showLapsHelpSheet = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                                    contentDescription = tyreWearHelpIconContentDescription,
                                    tint = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        },
                    )
                    ThresholdSlider(
                        value = uiState.tyreWearLaps.toFloat(),
                        valueRange = MINIMUM_PIT_TIMING_LAPS..MAXIMUM_PIT_TIMING_LAPS,
                        labelFormatter = { sliderLabel.format(it.roundToInt()) },
                        onValueChangeFinished = { onTyreWearLapsChanged(it.roundToInt()) },
                        steps = 3,
                        defaultValue = LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT.toFloat(),
                        onResetToDefault = {
                            onTyreWearLapsChanged(LMU_WINDOWS_PIT_TIMING_TYRE_WEAR_LAPS_DEFAULT)
                        },
                        resetContentDescription = resetToDefaultLabel,
                    )
                }
            },
        )
    }

    if (showLapsHelpSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLapsHelpSheet = false },
            sheetState = lapsHelpSheetState,
        ) {
            PitTimingLapsHelpSheetContent()
        }
    }
}

@Composable
internal fun PitTimingLapsHelpSheetContent(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.pit_timing_laps_help_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
    )
    Spacer(modifier = Modifier.height(24.dp))
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutPitTimingDetailPanePreview() {
    LmuWindowsReadoutPitTimingDetailPaneContent()
}
