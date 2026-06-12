package kurou.kodriver.feature.readout.vehicledamage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kodriver.feature.readout.vehicledamage.generated.resources.Res
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_description
import kodriver.feature.readout.vehicledamage.generated.resources.vehicle_damage_title
import kurou.kodriver.core.designsystem.DetailPaneDescription
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
        modifier = modifier,
    )
}

@Composable
internal fun VehicleDamageDetailPaneContent(
    uiState: VehicleDamageUiState,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        DetailPaneTitle(title = stringResource(Res.string.vehicle_damage_title))
        DetailPaneDescription(text = stringResource(Res.string.vehicle_damage_description))
    }
}

@Preview(showBackground = true)
@Composable
private fun VehicleDamageDetailPanePreview() {
    VehicleDamageDetailPaneContent(uiState = VehicleDamageUiState())
}
