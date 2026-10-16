package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.core.designsystem.SimulatorInfoContent as CommonSimulatorInfoContent

@Composable
internal fun SimulatorInfoContent() {
    CommonSimulatorInfoContent(simulatorId = Simulator.Gt7Ps5.id)
}
