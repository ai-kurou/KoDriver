package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_nearby_vehicle_distance
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_nearby_vehicles_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_left
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_right
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

private val SIDE_BY_SIDE_COLUMN_WIDTH = 80.dp

private fun formatMeters(value: LateralDistanceMeters): String {
    val rounded = round(value.value * 10) / 10
    return rounded.toString()
}

private fun formatMeters(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}

@Composable
internal fun SideBySideVehiclesContent(
    selectedSimulator: Simulator,
    vehicleApproach: LmuWindowsVehicleApproachData?,
    aceWindowsVehicleApproach: AceWindowsVehicleApproachData?,
) {
    when (selectedSimulator) {
        is Simulator.LmuWindows -> LmuWindowsSideBySideVehiclesContent(vehicleApproach)
        is Simulator.AceWindows -> AceWindowsNearbyVehiclesContent(aceWindowsVehicleApproach)
        is Simulator.Gt7Ps5 -> Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
    }
}

@Composable
private fun AceWindowsNearbyVehiclesContent(vehicleApproach: AceWindowsVehicleApproachData?) {
    if (vehicleApproach == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    if (vehicleApproach.nearbyVehicles.isEmpty()) {
        Text(text = stringResource(Res.string.debug_state_nearby_vehicles_none))
        return
    }
    Column {
        vehicleApproach.nearbyVehicles.sortedBy { it.distanceMeters }.forEach { nearbyVehicle ->
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_nearby_vehicle_distance,
                        formatMeters(nearbyVehicle.distanceMeters),
                    ),
            )
        }
    }
}

@Composable
private fun LmuWindowsSideBySideVehiclesContent(vehicleApproach: LmuWindowsVehicleApproachData?) {
    if (vehicleApproach == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    if (!vehicleApproach.isSideBySideLeft && !vehicleApproach.isSideBySideRight) {
        Text(text = stringResource(Res.string.debug_state_side_by_side_none))
        return
    }
    Row {
        Text(
            text =
                if (vehicleApproach.isSideBySideLeft) {
                    stringResource(
                        Res.string.debug_state_side_by_side_left,
                        formatMeters(vehicleApproach.lateralDistanceLeftMeters),
                    )
                } else {
                    ""
                },
            modifier = Modifier.width(SIDE_BY_SIDE_COLUMN_WIDTH),
        )
        Text(
            text =
                if (vehicleApproach.isSideBySideRight) {
                    stringResource(
                        Res.string.debug_state_side_by_side_right,
                        formatMeters(vehicleApproach.lateralDistanceRightMeters),
                    )
                } else {
                    ""
                },
            modifier = Modifier.width(SIDE_BY_SIDE_COLUMN_WIDTH),
        )
    }
}
