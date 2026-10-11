package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.core.designsystem.koDriverNumericTextStyle
import kurou.kodriver.domain.model.LateralDistanceMeters
import kurou.kodriver.domain.model.LmuWindowsPitState
import kurou.kodriver.domain.model.LmuWindowsPitStatusData
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.LmuWindowsTelemetryData
import kurou.kodriver.domain.model.LmuWindowsVehicleApproachData
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_flag_blue
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_flag_full_course_yellow
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_flag_none
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_flag_red
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_flag_yellow
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_fuel_consumption_per_lap_ratio
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_laps
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_fuel_consumption_remaining_percent
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_side_by_side_duration
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_side_by_side_left
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_side_by_side_none
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_side_by_side_right
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_unknown
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_in_garage_stall
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_in_pits
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_no
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_pit_state
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_entering
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_exiting
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_none
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_requested
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_pit_state_stopped
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_vehicle_location_yes
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.round

internal enum class ActiveRaceFlag(
    val labelRes: StringResource,
) {
    BLUE(Res.string.debug_state_flag_blue),
    YELLOW(Res.string.debug_state_flag_yellow),
    FULL_COURSE_YELLOW(Res.string.debug_state_flag_full_course_yellow),
    RED(Res.string.debug_state_flag_red),
}

internal fun determineActiveRaceFlags(raceFlags: LmuWindowsRaceFlagsData): List<ActiveRaceFlag> =
    buildList {
        if (raceFlags.playerFlag == PrimaryFlag.BLUE) add(ActiveRaceFlag.BLUE)
        if (raceFlags.playerUnderYellow || raceFlags.sectorFlags.any { it == SectorFlagState.YELLOW }) {
            add(ActiveRaceFlag.YELLOW)
        }
        if (raceFlags.gamePhase == SessionPhase.FULL_COURSE_YELLOW) add(ActiveRaceFlag.FULL_COURSE_YELLOW)
        if (raceFlags.gamePhase == SessionPhase.RED_FLAG) add(ActiveRaceFlag.RED)
    }

@Composable
internal fun LmuFlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
    if (raceFlags == null) {
        DebugStateUnavailableContent()
        return
    }
    val activeFlags = determineActiveRaceFlags(raceFlags)
    Column(verticalArrangement = Arrangement.spacedBy(KoDriverSpacing.extraSmall)) {
        if (activeFlags.isEmpty()) {
            DebugStateStatusChip(text = stringResource(Res.string.debug_state_flag_none))
        } else {
            activeFlags.forEach { flag ->
                val accent =
                    when (flag) {
                        ActiveRaceFlag.RED -> {
                            MaterialTheme.colorScheme.error
                        }

                        ActiveRaceFlag.YELLOW, ActiveRaceFlag.FULL_COURSE_YELLOW -> {
                            KoDriverExtendedColors.current.onWarningContainer
                        }

                        ActiveRaceFlag.BLUE -> {
                            MaterialTheme.colorScheme.tertiary
                        }
                    }
                DebugStateStatusChip(text = stringResource(flag.labelRes), accent = accent)
            }
        }
    }
}

@Composable
private fun lmuPitStateDisplayName(pitState: LmuWindowsPitState): String =
    when (pitState) {
        LmuWindowsPitState.NONE -> stringResource(Res.string.debug_state_vehicle_location_pit_state_none)
        LmuWindowsPitState.REQUESTED -> stringResource(Res.string.debug_state_vehicle_location_pit_state_requested)
        LmuWindowsPitState.ENTERING -> stringResource(Res.string.debug_state_vehicle_location_pit_state_entering)
        LmuWindowsPitState.STOPPED -> stringResource(Res.string.debug_state_vehicle_location_pit_state_stopped)
        LmuWindowsPitState.EXITING -> stringResource(Res.string.debug_state_vehicle_location_pit_state_exiting)
        LmuWindowsPitState.UNKNOWN -> stringResource(Res.string.debug_state_unknown)
    }

@Composable
private fun booleanDisplayName(value: Boolean): String =
    if (value) {
        stringResource(Res.string.debug_state_vehicle_location_yes)
    } else {
        stringResource(Res.string.debug_state_vehicle_location_no)
    }

@Composable
internal fun LmuWindowsVehicleLocationContent(lmuWindowsPitStatus: LmuWindowsPitStatusData?) {
    if (lmuWindowsPitStatus == null) {
        DebugStateUnavailableContent()
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

@Composable
internal fun LmuWindowsFuelContent(
    virtualEnergy: LmuWindowsVirtualEnergyData?,
    lmuWindowsTelemetry: LmuWindowsTelemetryData?,
) {
    val remainingPercent = calculateLmuVirtualEnergyRemainingPercent(virtualEnergy)
    if (remainingPercent == null) {
        DebugStateUnavailableContent()
        return
    }
    val consumption = calculateLmuVirtualEnergyConsumption(virtualEnergy, lmuWindowsTelemetry)
    Column {
        Text(
            text =
                stringResource(
                    Res.string.debug_state_fuel_consumption_remaining_percent,
                    formatOneDecimal(remainingPercent),
                ),
            style = koDriverNumericTextStyle(MaterialTheme.typography.headlineMedium),
        )
        if (consumption != null) {
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_per_lap_ratio,
                        formatOneDecimal(consumption.consumptionPerLap),
                    ),
                style = koDriverNumericTextStyle(),
            )
            Text(
                text =
                    stringResource(
                        Res.string.debug_state_fuel_consumption_remaining_laps,
                        formatOneDecimal(consumption.preciseRemainingLaps),
                    ),
                style = koDriverNumericTextStyle(),
            )
        }
    }
}

private val SIDE_BY_SIDE_COLUMN_WIDTH = 80.dp

private fun formatMeters(value: LateralDistanceMeters): String {
    val rounded = round(value.value * 10) / 10
    return rounded.toString()
}

@Composable
internal fun LmuWindowsSideBySideVehiclesContent(
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
