package kurou.kodriver.feature.lmuwindowsdebugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.domain.model.Simulator

@Composable
internal fun SimulatorInfoContent() {
    Text(text = simulatorDisplayName(Simulator.LmuWindows.id))
}
