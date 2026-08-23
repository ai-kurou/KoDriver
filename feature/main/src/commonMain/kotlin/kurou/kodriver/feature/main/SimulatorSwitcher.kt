package kurou.kodriver.feature.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.core.designsystem.simulatorIcon
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.simulator_switcher_description
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * `NavigationSuiteScaffold` の `primaryActionContent` に表示する、シミュレータ切替 UI の公開エントリーポイント。
 */
@Composable
fun SimulatorSwitcherContent(
    viewModel: AppScreenViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    SimulatorSwitcher(
        selectedSimulator = uiState.selectedSimulator,
        simulators = Simulator.entries,
        onSimulatorSelected = viewModel::selectSimulator,
        modifier = modifier,
    )
}

@Composable
internal fun SimulatorSwitcher(
    selectedSimulator: Simulator?,
    simulators: List<Simulator>,
    onSimulatorSelected: (Simulator) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val description = stringResource(Res.string.simulator_switcher_description)

    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            if (selectedSimulator != null) {
                Image(
                    painter = simulatorIcon(selectedSimulator.id),
                    contentDescription = description,
                    modifier = Modifier.size(28.dp).clip(CircleShape),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = description,
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            simulators.forEach { simulator ->
                DropdownMenuItem(
                    text = { Text(simulatorDisplayName(simulator.id)) },
                    leadingIcon = {
                        Image(
                            painter = simulatorIcon(simulator.id),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp).clip(RoundedCornerShape(4.dp)),
                        )
                    },
                    onClick = {
                        onSimulatorSelected(simulator)
                        expanded = false
                    },
                )
            }
        }
    }
}
