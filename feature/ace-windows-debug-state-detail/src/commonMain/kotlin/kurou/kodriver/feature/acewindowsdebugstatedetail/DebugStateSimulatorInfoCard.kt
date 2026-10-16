package kurou.kodriver.feature.acewindowsdebugstatedetail

import androidx.compose.runtime.Composable
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.core.designsystem.SimulatorInfoContent as CommonSimulatorInfoContent

@Composable
internal fun SimulatorInfoContent() {
    CommonSimulatorInfoContent(simulatorId = Simulator.AceWindows.id)
}
