package kurou.kodriver.feature.readout.vehicleapproach

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun VehicleApproachDetailPane(
    modifier: Modifier = Modifier,
) {
    VehicleApproachDetailPane(
        modifier = modifier,
        viewModel = koinViewModel(),
    )
}

@Composable
internal fun VehicleApproachDetailPane(
    modifier: Modifier = Modifier,
    viewModel: VehicleApproachViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    VehicleApproachDetailPaneContent(uiState = uiState, modifier = modifier)
}

@Composable
internal fun VehicleApproachDetailPaneContent(
    uiState: VehicleApproachUiState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "車両接近",
            style = MaterialTheme.typography.titleLarge,
        )
        ProximityCard(
            side = "左",
            isSideBySide = uiState.isSideBySideLeft,
            lateralDistanceMeters = uiState.lateralDistanceLeftMeters,
        )
        ProximityCard(
            side = "右",
            isSideBySide = uiState.isSideBySideRight,
            lateralDistanceMeters = uiState.lateralDistanceRightMeters,
        )
    }
}

@Composable
private fun ProximityCard(
    side: String,
    isSideBySide: Boolean,
    lateralDistanceMeters: Double?,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = side,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isSideBySide) "並走中" else "並走なし",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSideBySide)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (lateralDistanceMeters != null) {
                    Text(
                        text = "%.1f m".format(lateralDistanceMeters),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun VehicleApproachDetailPanePreview() {
    VehicleApproachDetailPaneContent(
        uiState = VehicleApproachUiState(
            isSideBySideLeft = true,
            isSideBySideRight = false,
            lateralDistanceLeftMeters = 1.2,
            lateralDistanceRightMeters = null,
        ),
    )
}
