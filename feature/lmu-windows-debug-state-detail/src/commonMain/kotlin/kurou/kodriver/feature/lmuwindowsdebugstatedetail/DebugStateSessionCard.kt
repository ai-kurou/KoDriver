package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.DebugStateUnavailableContent
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_SESSION_PRACTICE_RANGE
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_SESSION_QUALIFYING_RANGE
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_SESSION_RACE_RANGE
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_SESSION_TEST_DAY
import kurou.kodriver.domain.lmuwindows.model.LMU_WINDOWS_SESSION_WARMUP
import kurou.kodriver.domain.lmuwindows.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_session_practice
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_session_qualifying
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_session_race
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_session_test_day
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_session_warmup
import kurou.kodriver.feature.lmuwindowsdebugstatedetail.generated.resources.debug_state_unknown
import org.jetbrains.compose.resources.stringResource

@Composable
private fun sessionDisplayName(session: Int): String =
    when (session) {
        LMU_WINDOWS_SESSION_TEST_DAY -> stringResource(Res.string.debug_state_session_test_day)
        in LMU_WINDOWS_SESSION_PRACTICE_RANGE -> stringResource(Res.string.debug_state_session_practice)
        in LMU_WINDOWS_SESSION_QUALIFYING_RANGE -> stringResource(Res.string.debug_state_session_qualifying)
        LMU_WINDOWS_SESSION_WARMUP -> stringResource(Res.string.debug_state_session_warmup)
        in LMU_WINDOWS_SESSION_RACE_RANGE -> stringResource(Res.string.debug_state_session_race)
        else -> stringResource(Res.string.debug_state_unknown)
    }

@Composable
internal fun SessionContent(virtualEnergy: LmuWindowsVirtualEnergyData?) {
    if (virtualEnergy == null) {
        DebugStateUnavailableContent()
        return
    }
    Text(text = sessionDisplayName(virtualEnergy.session))
}
