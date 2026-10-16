package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.Res
import kurou.kodriver.feature.acewindowsdebugstatedetail.generated.resources.debug_state_flag_info_unavailable
import org.jetbrains.compose.resources.stringResource
import kurou.kodriver.core.designsystem.DebugStateUnavailableContent as CommonDebugStateUnavailableContent

@Composable
internal fun DebugStateUnavailableContent() {
    CommonDebugStateUnavailableContent(text = stringResource(Res.string.debug_state_flag_info_unavailable))
}
