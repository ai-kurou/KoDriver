package kurou.kodriver.feature.readout.vehicledamage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.readout.vehicledamage.generated.resources.Res
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_description
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_overheat_subtitle
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_overheat_switch_label
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_title
import kurou.kodriver.core.designsystem.DetailPaneDescription
import kurou.kodriver.core.designsystem.DetailPaneSubtitle
import kurou.kodriver.core.designsystem.DetailPaneTitle
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VehicleDamageDetailPane(
    modifier: Modifier = Modifier,
) {
    val viewModel: VehicleDamageViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    VehicleDamageDetailPaneContent(
        uiState = uiState,
        onOverheatEnabledChanged = viewModel::onOverheatEnabledChanged,
        modifier = modifier,
    )
}

@Composable
internal fun VehicleDamageDetailPaneContent(
    uiState: VehicleDamageUiState,
    onOverheatEnabledChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.vehicle_damage_title))
        DetailPaneDescription(text = stringResource(Res.string.vehicle_damage_description))
        DetailPaneSubtitle(text = stringResource(Res.string.vehicle_damage_overheat_subtitle))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            Text(
                text = stringResource(Res.string.vehicle_damage_overheat_switch_label),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = uiState.overheatEnabled,
                onCheckedChange = onOverheatEnabledChanged,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VehicleDamageDetailPanePreview() {
    VehicleDamageDetailPaneContent(uiState = VehicleDamageUiState())
}
