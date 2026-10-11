package kurou.kodriver.feature.gt7ps5debugstatedetail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.domain.model.Simulator

@Composable
internal fun SimulatorInfoContent() {
    Text(text = simulatorDisplayName(Simulator.Gt7Ps5.id))
}
