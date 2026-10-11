package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.AceWindowsFuelData
import kurou.kodriver.domain.model.AceWindowsRemainingFuelLapsData
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.AceWindowsStatusType
import kurou.kodriver.domain.model.AceWindowsVehicleApproachData
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_black
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_black_white
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_blue
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_checkered
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_green
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_orange_circle
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red_yellow_stripes
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_white
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_yellow
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_laps
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_percent
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_nearby_vehicle_distance
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_nearby_vehicles_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_pitentry
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_pitexit
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_pitlane
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_status
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_status_live
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_status_off
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_status_pause
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_status_replay
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_status_unknown
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_track
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_unassigned
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_unknown
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

@Composable
private fun aceFlagDisplayName(flag: AceWindowsFlagType): String =
    when (flag) {
        AceWindowsFlagType.NO_FLAG -> stringResource(Res.string.debug_state_flag_none)
        AceWindowsFlagType.WHITE_FLAG -> stringResource(Res.string.debug_state_flag_white)
        AceWindowsFlagType.GREEN_FLAG -> stringResource(Res.string.debug_state_flag_green)
        AceWindowsFlagType.RED_FLAG -> stringResource(Res.string.debug_state_flag_red)
        AceWindowsFlagType.BLUE_FLAG -> stringResource(Res.string.debug_state_flag_blue)
        AceWindowsFlagType.YELLOW_FLAG -> stringResource(Res.string.debug_state_flag_yellow)
        AceWindowsFlagType.BLACK_FLAG -> stringResource(Res.string.debug_state_flag_black)
        AceWindowsFlagType.BLACK_WHITE_FLAG -> stringResource(Res.string.debug_state_flag_black_white)
        AceWindowsFlagType.CHECKERED_FLAG -> stringResource(Res.string.debug_state_flag_checkered)
        AceWindowsFlagType.ORANGE_CIRCLE_FLAG -> stringResource(Res.string.debug_state_flag_orange_circle)
        AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG -> stringResource(Res.string.debug_state_flag_red_yellow_stripes)
        AceWindowsFlagType.UNKNOWN -> stringResource(Res.string.debug_state_flag_info_unavailable)
    }

@Composable
internal fun AceFlagInfoContent(aceWindowsFlag: AceWindowsFlagData?) {
    if (aceWindowsFlag == null) {
        DebugStateUnavailableContent()
        return
    }
    val displayText = aceFlagDisplayName(aceWindowsFlag.flag)
    if (aceWindowsFlag.flag == AceWindowsFlagType.UNKNOWN) {
        DebugStateUnavailableContent()
    } else {
        val accent =
            when (aceWindowsFlag.flag) {
                AceWindowsFlagType.RED_FLAG -> MaterialTheme.colorScheme.error

                AceWindowsFlagType.GREEN_FLAG -> MaterialTheme.colorScheme.primary

                AceWindowsFlagType.YELLOW_FLAG, AceWindowsFlagType.ORANGE_CIRCLE_FLAG,
                AceWindowsFlagType.RED_YELLOW_STRIPES_FLAG,
                -> KoDriverExtendedColors.current.onWarningContainer

                else -> MaterialTheme.colorScheme.tertiary
            }
        DebugStateStatusChip(text = displayText, accent = accent)
    }
}

@Composable
private fun aceCarLocationDisplayName(carLocation: AceWindowsCarLocation): String =
    when (carLocation) {
        AceWindowsCarLocation.UNASSIGNED -> stringResource(Res.string.debug_state_vehicle_location_ace_unassigned)
        AceWindowsCarLocation.PITLANE -> stringResource(Res.string.debug_state_vehicle_location_ace_pitlane)
        AceWindowsCarLocation.PITENTRY -> stringResource(Res.string.debug_state_vehicle_location_ace_pitentry)
        AceWindowsCarLocation.PITEXIT -> stringResource(Res.string.debug_state_vehicle_location_ace_pitexit)
        AceWindowsCarLocation.TRACK -> stringResource(Res.string.debug_state_vehicle_location_ace_track)
        AceWindowsCarLocation.UNKNOWN -> stringResource(Res.string.debug_state_vehicle_location_ace_unknown)
    }

@Composable
private fun aceStatusTypeDisplayName(status: AceWindowsStatusType): String =
    when (status) {
        AceWindowsStatusType.OFF -> stringResource(Res.string.debug_state_vehicle_location_ace_status_off)
        AceWindowsStatusType.REPLAY -> stringResource(Res.string.debug_state_vehicle_location_ace_status_replay)
        AceWindowsStatusType.LIVE -> stringResource(Res.string.debug_state_vehicle_location_ace_status_live)
        AceWindowsStatusType.PAUSE -> stringResource(Res.string.debug_state_vehicle_location_ace_status_pause)
        AceWindowsStatusType.UNKNOWN -> stringResource(Res.string.debug_state_vehicle_location_ace_status_unknown)
    }

@Composable
internal fun AceWindowsVehicleLocationContent(aceWindowsStatus: AceWindowsStatusData?) {
    if (aceWindowsStatus == null) {
        DebugStateUnavailableContent()
        return
    }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_vehicle_location_ace_status,
                    aceStatusTypeDisplayName(aceWindowsStatus.status),
                ),
        )
        Text(text = aceCarLocationDisplayName(aceWindowsStatus.carLocation))
    }
}

@Composable
internal fun AceWindowsFuelContent(
    aceWindowsFuel: AceWindowsFuelData?,
    aceWindowsRemainingFuelLaps: AceWindowsRemainingFuelLapsData?,
) {
    if (aceWindowsFuel == null) {
        DebugStateUnavailableContent()
        return
    }
    // ACE は消費実績がない間 remainingLaps が 0 のため、正の有限値のときだけ残り周数を表示する。
    val remainingLaps = aceWindowsRemainingFuelLaps?.remainingLaps?.takeIf { it.isFinite() && it > 0f }
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_fuel_consumption_remaining_percent,
                    formatOneDecimal(aceWindowsFuel.remainingPercent.value),
                ),
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
        if (remainingLaps != null) {
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_remaining_laps,
                        formatOneDecimal(remainingLaps.toDouble()),
                    ),
                style = koDriverNumericTextStyle(),
            )
        }
    }
}

private fun formatMeters(value: Double): String {
    val rounded = round(value * 10) / 10
    return rounded.toString()
}

@Composable
internal fun AceWindowsNearbyVehiclesContent(vehicleApproach: AceWindowsVehicleApproachData?) {
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
