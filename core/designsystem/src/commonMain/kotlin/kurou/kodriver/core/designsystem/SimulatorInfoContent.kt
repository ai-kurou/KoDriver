package kurou.kodriver.core.designsystem

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** シミュレーターIDに対応する表示名を表示する。 */
@Composable
fun SimulatorInfoContent(
    simulatorId: String,
    modifier: Modifier = Modifier,
) {
    Text(text = simulatorDisplayName(simulatorId), modifier = modifier)
}
