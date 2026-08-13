package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.LmuWindowsVirtualEnergyData
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_flag_info_unavailable
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_practice
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_qualifying
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_race
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_test_day
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_unknown
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_session_warmup
import org.jetbrains.compose.resources.stringResource

@Composable
private fun sessionDisplayName(session: Int): String =
    when (session) {
        0 -> stringResource(Res.string.debug_state_session_test_day)
        in 1..4 -> stringResource(Res.string.debug_state_session_practice)
        in 5..8 -> stringResource(Res.string.debug_state_session_qualifying)
        9 -> stringResource(Res.string.debug_state_session_warmup)
        in 10..13 -> stringResource(Res.string.debug_state_session_race)
        else -> stringResource(Res.string.debug_state_session_unknown)
    }

@Composable
internal fun SessionContent(virtualEnergy: LmuWindowsVirtualEnergyData?) {
    if (virtualEnergy == null) {
        Text(text = stringResource(Res.string.debug_state_flag_info_unavailable))
        return
    }
    Text(text = sessionDisplayName(virtualEnergy.session))
}
