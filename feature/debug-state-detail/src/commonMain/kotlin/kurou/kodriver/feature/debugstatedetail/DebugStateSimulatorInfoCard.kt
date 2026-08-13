package kurou.kodriver.feature.debugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.debugstatedetail.generated.resources.Res
import kurou.kodriver.feature.debugstatedetail.generated.resources.debug_state_simulator_info_unselected
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SimulatorInfoContent(selectedSimulator: Simulator?) {
    Text(
        text =
            selectedSimulator
                ?.let { simulatorDisplayName(it.id) }
                ?: stringResource(Res.string.debug_state_simulator_info_unselected),
    )
}
