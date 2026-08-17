package kurou.kodriver.feature.debugstatedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.SessionYellowFlagState
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_invalid
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_last_lap
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_none
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pending
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pit_closed
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pit_lead_lap
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_pit_open
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_race_halt
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_resume
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_yellow_flag_state_unknown
import org.jetbrains.compose.resources.stringResource

@Composable
private fun yellowFlagStateDisplayName(yellowFlagState: SessionYellowFlagState): String =
    when (yellowFlagState) {
        SessionYellowFlagState.INVALID -> stringResource(Res.string.debug_state_yellow_flag_state_invalid)
        SessionYellowFlagState.NONE -> stringResource(Res.string.debug_state_yellow_flag_state_none)
        SessionYellowFlagState.PENDING -> stringResource(Res.string.debug_state_yellow_flag_state_pending)
        SessionYellowFlagState.PIT_CLOSED -> stringResource(Res.string.debug_state_yellow_flag_state_pit_closed)
        SessionYellowFlagState.PIT_LEAD_LAP -> stringResource(Res.string.debug_state_yellow_flag_state_pit_lead_lap)
        SessionYellowFlagState.PIT_OPEN -> stringResource(Res.string.debug_state_yellow_flag_state_pit_open)
        SessionYellowFlagState.LAST_LAP -> stringResource(Res.string.debug_state_yellow_flag_state_last_lap)
        SessionYellowFlagState.RESUME -> stringResource(Res.string.debug_state_yellow_flag_state_resume)
        SessionYellowFlagState.RACE_HALT -> stringResource(Res.string.debug_state_yellow_flag_state_race_halt)
        SessionYellowFlagState.UNKNOWN -> stringResource(Res.string.debug_state_yellow_flag_state_unknown)
    }

@Composable
internal fun YellowFlagStateContent(raceFlags: LmuWindowsRaceFlagsData?) {
    val displayText =
        if (raceFlags == null) {
            stringResource(Res.string.debug_state_flag_info_unavailable)
        } else {
            yellowFlagStateDisplayName(raceFlags.yellowFlagState)
        }
    AnimatedContent(
        targetState = displayText,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { text ->
        Text(text = text)
    }
}
