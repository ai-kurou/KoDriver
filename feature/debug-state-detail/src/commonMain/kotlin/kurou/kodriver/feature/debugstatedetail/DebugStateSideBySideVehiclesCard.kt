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
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_nearby_vehicle_distance
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_nearby_vehicles_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_side_by_side_duration
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
    durations: LmuWindowsSideBySideDurations?,
) {
    when (selectedSimulator) {
        is Simulator.LmuWindows -> LmuWindowsSideBySideVehiclesContent(vehicleApproach, durations)
        is Simulator.AceWindows -> AceWindowsNearbyVehiclesContent(aceWindowsVehicleApproach)
        is Simulator.Gt7Ps5 -> DebugStateUnavailableContent()
    }
}

@Composable
private fun AceWindowsNearbyVehiclesContent(vehicleApproach: AceWindowsVehicleApproachData?) {
    if (vehicleApproach == null) {
        DebugStateUnavailableContent()
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
private fun LmuWindowsSideBySideVehiclesContent(
    vehicleApproach: LmuWindowsVehicleApproachData?,
    durations: LmuWindowsSideBySideDurations?,
) {
    if (vehicleApproach == null) {
        DebugStateUnavailableContent()
        return
    }
    if (!vehicleApproach.isSideBySideLeft && !vehicleApproach.isSideBySideRight) {
        Text(text = stringResource(Res.string.debug_state_side_by_side_none))
        return
    }
    Row {
        Column(modifier = Modifier.width(SIDE_BY_SIDE_COLUMN_WIDTH)) {
            if (vehicleApproach.isSideBySideLeft) {
                Text(
                    text =
                        stringResource(
                            Res.string.debug_state_side_by_side_left,
                            formatMeters(vehicleApproach.lateralDistanceLeftMeters),
                        ),
                )
                durations?.leftMillis?.let { SideBySideDurationContent(it) }
            }
        }
        Column(modifier = Modifier.width(SIDE_BY_SIDE_COLUMN_WIDTH)) {
            if (vehicleApproach.isSideBySideRight) {
                Text(
                    text =
                        stringResource(
                            Res.string.debug_state_side_by_side_right,
                            formatMeters(vehicleApproach.lateralDistanceRightMeters),
                        ),
                )
                durations?.rightMillis?.let { SideBySideDurationContent(it) }
            }
        }
    }
}

@Composable
private fun SideBySideDurationContent(millis: Long) {
    val seconds = round(millis / 100.0) / 10
    Text(text = stringResource(Res.string.debug_state_side_by_side_duration, seconds.toString()))
}
