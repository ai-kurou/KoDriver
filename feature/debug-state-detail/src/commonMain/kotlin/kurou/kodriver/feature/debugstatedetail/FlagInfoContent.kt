package kurou.kodriver.feature.debugstatedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.KoDriverExtendedColors
import kurou.kodriver.core.designsystem.KoDriverSpacing
import kurou.kodriver.domain.model.AceWindowsFlagData
import kurou.kodriver.domain.model.AceWindowsFlagType
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.PrimaryFlag
import kurou.kodriver.domain.model.SectorFlagState
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_black
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_black_white
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_blue
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_checkered
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_full_course_yellow
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_green
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_orange_circle
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_red_yellow_stripes
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_white
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_yellow
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

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
internal fun FlagInfoContent(
    selectedSimulator: Simulator,
    raceFlags: LmuWindowsRaceFlagsData?,
    aceWindowsFlag: AceWindowsFlagData?,
) {
    AnimatedContent(
        targetState = Triple(selectedSimulator, raceFlags, aceWindowsFlag),
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { (simulator, lmuFlags, aceFlag) ->
        FlagInfoStaticContent(simulator, lmuFlags, aceFlag)
    }
}

@Composable
private fun FlagInfoStaticContent(
    selectedSimulator: Simulator,
    raceFlags: LmuWindowsRaceFlagsData?,
    aceWindowsFlag: AceWindowsFlagData?,
) {
    when (selectedSimulator) {
        is Simulator.LmuWindows -> {
            LmuFlagInfoContent(raceFlags)
        }

        is Simulator.AceWindows -> {
            AceFlagInfoContent(aceWindowsFlag)
        }

        is Simulator.Gt7Ps5 -> {
            DebugStateUnavailableContent()
        }
    }
}

@Composable
private fun LmuFlagInfoContent(raceFlags: LmuWindowsRaceFlagsData?) {
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
private fun AceFlagInfoContent(aceWindowsFlag: AceWindowsFlagData?) {
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
