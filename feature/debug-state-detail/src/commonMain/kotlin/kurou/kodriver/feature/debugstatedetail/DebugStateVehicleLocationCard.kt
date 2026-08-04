package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.AceWindowsCarLocation
import kurou.kodriver.domain.model.AceWindowsStatusData
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_pitentry
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_pitexit
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_pitlane
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_track
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_unassigned
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_ace_unknown
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_in_garage_stall
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_in_pits
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_no
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_entering
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_exiting
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_requested
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_stopped
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_unknown
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_vehicle_location_yes
import org.jetbrains.compose.resources.stringResource

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
private fun lmuPitStateDisplayName(pitState: LmuWindowsPitState): String =
    when (pitState) {
        LmuWindowsPitState.NONE -> stringResource(Res.string.debug_state_vehicle_location_pit_state_none)
        LmuWindowsPitState.REQUESTED -> stringResource(Res.string.debug_state_vehicle_location_pit_state_requested)
        LmuWindowsPitState.ENTERING -> stringResource(Res.string.debug_state_vehicle_location_pit_state_entering)
        LmuWindowsPitState.STOPPED -> stringResource(Res.string.debug_state_vehicle_location_pit_state_stopped)
        LmuWindowsPitState.EXITING -> stringResource(Res.string.debug_state_vehicle_location_pit_state_exiting)
        LmuWindowsPitState.UNKNOWN -> stringResource(Res.string.debug_state_vehicle_location_pit_state_unknown)
    }

@Composable
private fun booleanDisplayName(value: Boolean): String =
    if (value) {
        stringResource(Res.string.debug_state_vehicle_location_yes)
    } else {
        stringResource(Res.string.debug_state_vehicle_location_no)
    }

@Composable
internal fun VehicleLocationContent(
    selectedSimulator: Simulator?,
    aceWindowsStatus: AceWindowsStatusData?,
    lmuWindowsPitStatus: LmuWindowsPitStatusData?,
) {
    when (selectedSimulator) {
        is Simulator.AceWindows -> {
            val carLocation = aceWindowsStatus?.carLocation
            if (carLocation == null) {
                Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
                return
            }
            Text(text = aceCarLocationDisplayName(carLocation))
        }

        is Simulator.LmuWindows -> {
            if (lmuWindowsPitStatus == null) {
                Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
                return
            }
            Column {
                Text(
                    text =
                        stringResource(
                            Res.string.debug_state_vehicle_location_in_pits,
                            booleanDisplayName(lmuWindowsPitStatus.inPits),
                        ),
                )
                Text(
                    text =
                        stringResource(
                            Res.string.debug_state_vehicle_location_pit_state,
                            lmuPitStateDisplayName(lmuWindowsPitStatus.pitState),
                        ),
                )
                Text(
                    text =
                        stringResource(
                            Res.string.debug_state_vehicle_location_in_garage_stall,
                            booleanDisplayName(lmuWindowsPitStatus.inGarageStall),
                        ),
                )
            }
        }

        is Simulator.Gt7Ps5, null -> {
            Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        }
    }
}
