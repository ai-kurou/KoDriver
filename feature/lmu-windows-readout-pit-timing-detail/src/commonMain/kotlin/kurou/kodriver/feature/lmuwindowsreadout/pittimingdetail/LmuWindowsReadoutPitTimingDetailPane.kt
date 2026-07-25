package kurou.kodriver.feature.lmuwindowsreadout.pittimingdetail

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
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.Res
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_description
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_tyre_wear_title
import kodriver.feature.lmuwindowsreadout.pittimingdetail.generated.resources.pit_timing_virtual_energy_title
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LmuWindowsReadoutPitTimingDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: LmuWindowsReadoutPitTimingDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LmuWindowsReadoutPitTimingDetailPaneContent(
        uiState = uiState,
        onVirtualEnergyEnabledChanged = viewModel::onVirtualEnergyEnabledChanged,
        onTyreWearEnabledChanged = viewModel::onTyreWearEnabledChanged,
        modifier = modifier,
    )
}

@Composable
internal fun LmuWindowsReadoutPitTimingDetailPaneContent(
    uiState: LmuWindowsReadoutPitTimingDetailUiState = LmuWindowsReadoutPitTimingDetailUiState(),
    onVirtualEnergyEnabledChanged: (Boolean) -> Unit = {},
    onTyreWearEnabledChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
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
            checked = uiState.virtualEnergyEnabled,
            onCheckedChange = onVirtualEnergyEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {},
        )
        DetailPaneCard(
            title = stringResource(Res.string.pit_timing_tyre_wear_title),
            checked = uiState.tyreWearEnabled,
            onCheckedChange = onTyreWearEnabledChanged,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            bottomContent = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LmuWindowsReadoutPitTimingDetailPanePreview() {
    LmuWindowsReadoutPitTimingDetailPaneContent()
}
