package kurou.kodriver.feature.debugstatedetail

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kodriver.feature.debugstatedetail.generated.resources.Res
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_black
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_black_white
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_blue
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_checkered
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_full_course_yellow
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_green
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_none
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_orange_circle
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red_yellow_stripes
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_white
import kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_yellow
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.Simulator
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

internal enum class ActiveRaceFlag(val labelRes: StringResource) {
    BLUE(Res.string.debug_state_flag_blue),
    YELLOW(Res.string.debug_state_flag_yellow),
    FULL_COURSE_YELLOW(Res.string.debug_state_flag_full_course_yellow),
    RED(Res.string.debug_state_flag_red),
}

internal fun determineActiveRaceFlags(raceFlags: LmuWindowsRaceFlagsData): List<ActiveRaceFlag> = buildList {
    if (raceFlags.playerFlag == PrimaryFlag.BLUE) add(ActiveRaceFlag.BLUE)
    if (raceFlags.playerUnderYellow || raceFlags.sectorFlags.any { it == SectorFlagState.YELLOW }) {
        add(ActiveRaceFlag.YELLOW)
    }
    if (raceFlags.gamePhase == SessionPhase.FULL_COURSE_YELLOW) add(ActiveRaceFlag.FULL_COURSE_YELLOW)
    if (raceFlags.gamePhase == SessionPhase.RED_FLAG) add(ActiveRaceFlag.RED)
}

@Composable
internal fun FlagInfoContent(
    selectedSimulator: Simulator?,
    raceFlags: LmuWindowsRaceFlagsData?,
    aceWindowsFlag: AceWindowsFlagData?,
) {
    when (selectedSimulator) {
        is Simulator.LmuWindows -> LmuFlagInfoContent(raceFlags)
        is Simulator.AceWindows -> AceFlagInfoContent(aceWindowsFlag)
        is Simulator.Gt7Ps5, null -> Text(
            text = stringResource(Res.string.debug_state_flag_info_unavailable),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LmuFlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
    if (raceFlags == null) {
        Text(
            text = stringResource(Res.string.debug_state_flag_info_unavailable),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    val activeFlags = determineActiveRaceFlags(raceFlags)
    Column {
        if (activeFlags.isEmpty()) {
            Text(
                text = stringResource(Res.string.debug_state_flag_none),
                style = MaterialTheme.typography.bodyMedium,
            )
        } else {
            activeFlags.forEach { flag ->
                Text(text = stringResource(flag.labelRes), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun aceFlagDisplayName(flag: AceWindowsFlagType): String = when (flag) {
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
private fun AceFlagInfoContent(aceWindowsFlag: AceWindowsFlagData?) {
    if (aceWindowsFlag == null) {
        Text(
            text = stringResource(Res.string.debug_state_flag_info_unavailable),
            style = MaterialTheme.typography.bodyMedium,
        )
        return
    }
    Text(text = aceFlagDisplayName(aceWindowsFlag.flag), style = MaterialTheme.typography.bodyMedium)
}
