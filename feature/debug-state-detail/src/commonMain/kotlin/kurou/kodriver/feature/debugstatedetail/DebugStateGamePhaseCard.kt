package kurou.kodriver.feature.debugstatedetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsRaceFlagsData
import kurou.kodriver.domain.model.SessionPhase
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_countdown
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_formation
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_full_course_yellow
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_garage
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_green_flag
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_grid_walk
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_paused
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_session_over
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_session_stopped
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_unknown
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_game_phase_warm_up
import org.jetbrains.compose.resources.stringResource

@Composable
private fun gamePhaseDisplayName(gamePhase: SessionPhase): String =
    when (gamePhase) {
        SessionPhase.GARAGE -> stringResource(Res.string.debug_state_game_phase_garage)
        SessionPhase.WARM_UP -> stringResource(Res.string.debug_state_game_phase_warm_up)
        SessionPhase.GRID_WALK -> stringResource(Res.string.debug_state_game_phase_grid_walk)
        SessionPhase.FORMATION -> stringResource(Res.string.debug_state_game_phase_formation)
        SessionPhase.COUNTDOWN -> stringResource(Res.string.debug_state_game_phase_countdown)
        SessionPhase.GREEN_FLAG -> stringResource(Res.string.debug_state_game_phase_green_flag)
        SessionPhase.FULL_COURSE_YELLOW -> stringResource(Res.string.debug_state_game_phase_full_course_yellow)
        SessionPhase.RED_FLAG -> stringResource(Res.string.debug_state_game_phase_session_stopped)
        SessionPhase.SESSION_OVER -> stringResource(Res.string.debug_state_game_phase_session_over)
        SessionPhase.PAUSED_OR_HEARTBEAT -> stringResource(Res.string.debug_state_game_phase_paused)
        SessionPhase.UNKNOWN -> stringResource(Res.string.debug_state_game_phase_unknown)
    }

@Composable
internal fun GamePhaseContent(raceFlags: LmuWindowsRaceFlagsData?) {
    val displayText =
        if (raceFlags == null) {
            stringResource(Res.string.debug_state_flag_info_unavailable)
        } else {
            gamePhaseDisplayName(raceFlags.gamePhase)
        }
    AnimatedContent(
        targetState = displayText,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { text ->
        Text(text = text)
    }
}
