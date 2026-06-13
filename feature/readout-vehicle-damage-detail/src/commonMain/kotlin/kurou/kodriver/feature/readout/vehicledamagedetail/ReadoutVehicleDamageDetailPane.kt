package kurou.kodriver.feature.readout.vehicledamagedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.readout.vehicledamagedetail.generated.resources.Res
import kodriver.feature.readout.vehicledamagedetail.generated.resources.vehicle_damage_description
import kodriver.feature.readout.vehicledamagedetail.generated.resources.vehicle_damage_overheat_chip_label
import kodriver.feature.readout.vehicledamagedetail.generated.resources.vehicle_damage_overheat_subtitle
import kodriver.feature.readout.vehicledamagedetail.generated.resources.vehicle_damage_overheat_switch_label
import kurou.kodriver.core.designsystem.DetailPaneCard
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReadoutVehicleDamageDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: ReadoutVehicleDamageDetailViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ReadoutVehicleDamageDetailPaneContent(
        uiState = uiState,
        onOverheatEnabledChanged = viewModel::onOverheatEnabledChanged,
        modifier = modifier,
    )
}

@Composable
internal fun ReadoutVehicleDamageDetailPaneContent(
    uiState: ReadoutVehicleDamageDetailUiState,
    onOverheatEnabledChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneDescription(text = stringResource(Res.string.vehicle_damage_description))
        DetailPaneSubtitle(text = stringResource(Res.string.vehicle_damage_overheat_subtitle))
        val chipLabel = stringResource(Res.string.vehicle_damage_overheat_chip_label)
        DetailPaneCard(
            title = stringResource(Res.string.vehicle_damage_overheat_switch_label),
            checked = uiState.overheatEnabled,
            chipLabels = listOf(chipLabel),
            selectedChipLabels = setOf(chipLabel),
            onCheckedChange = onOverheatEnabledChanged,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ReadoutVehicleDamageDetailPanePreview() {
    ReadoutVehicleDamageDetailPaneContent(uiState = ReadoutVehicleDamageDetailUiState())
}
