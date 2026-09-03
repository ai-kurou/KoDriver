package kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneCardChips
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_description
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_overheat_chip_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_overheat_switch_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_part_detached_chip_label
import kurou.kodriver.feature.lmuwindowsreadout.vehicledamagedetail.generated.resources.vehicle_damage_part_detached_switch_label
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * LmuWindowsReadoutVehicleDamageDetail の画面を表示する Composable。
 */
@Composable
fun LmuWindowsReadoutVehicleDamageDetailPane(modifier: Modifier = Modifier) {
    val viewModel: LmuWindowsReadoutVehicleDamageDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutVehicleDamageDetailPaneContent(
        uiState = uiState,
        onOverheatEnabledChanged = viewModel::onOverheatEnabledChanged,
        onPreviewClicked = viewModel::onPreviewClicked,
        onPartDetachedEnabledChanged = viewModel::onPartDetachedEnabledChanged,
        onPartDetachedPreviewClicked = viewModel::onPartDetachedPreviewClicked,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutVehicleDamageDetailPaneContent(
    uiState: LmuWindowsReadoutVehicleDamageDetailUiState,
    onOverheatEnabledChanged: (Boolean) -> Unit = {},
    onPreviewClicked: () -> Unit = {},
    onPartDetachedEnabledChanged: (Boolean) -> Unit = {},
    onPartDetachedPreviewClicked: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
    ) {
        DetailPaneDescription(
            text = stringResource(Res.string.vehicle_damage_description),
        )
        val chipLabel = stringResource(Res.string.vehicle_damage_overheat_chip_label)
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_damage_overheat_switch_label),
            checked = uiState.overheatEnabled,
            onCheckedChange = onOverheatEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = listOf(chipLabel),
                    selectedChipLabels = setOf(chipLabel),
                    chipEnabled = uiState.overheatEnabled,
                    onChipClick = { onPreviewClicked() },
                )
            },
        )
        val partDetachedChipLabel = stringResource(Res.string.vehicle_damage_part_detached_chip_label)
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_damage_part_detached_switch_label),
            checked = uiState.partDetachedEnabled,
            onCheckedChange = onPartDetachedEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {
                DetailPaneCardChips(
                    chipLabels = listOf(partDetachedChipLabel),
                    selectedChipLabels = setOf(partDetachedChipLabel),
                    chipEnabled = uiState.partDetachedEnabled,
                    onChipClick = { onPartDetachedPreviewClicked() },
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutVehicleDamageDetailPanePreview() {
    LmuWindowsReadoutVehicleDamageDetailPaneContent(uiState = LmuWindowsReadoutVehicleDamageDetailUiState())
}
