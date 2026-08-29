package kurou.kodriver.feature.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kurou.kodriver.core.designsystem.simulatorDisplayName
import kurou.kodriver.core.designsystem.simulatorIcon
import kurou.kodriver.core.designsystem.simulatorShortName
import kurou.kodriver.domain.model.Simulator
import kurou.kodriver.feature.main.generated.resources.Res
import kurou.kodriver.feature.main.generated.resources.select_simulator_hint
import org.jetbrains.compose.resources.stringResource

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータのアイコン。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun AppScreenPrimarySimulatorIcon(
    simulatorId: String,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = simulatorIcon(simulatorId),
        contentDescription = stringResource(Res.string.select_simulator_hint),
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
    )
}

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータの短縮名（"LMU"・"GT7"・"ACE"）。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun appScreenPrimarySimulatorLabel(simulatorId: String): String = simulatorShortName(simulatorId)

/**
 * NavigationRail / NavigationBar の先頭項目に表示する、現在選択中のシミュレータのアイコンと、
 * それをタップして開くシミュレータ選択メニュー。
 * [simulatorId] は `kurou.kodriver.domain.model.Simulator.id` の値と一致させる必要がある。
 */
@Composable
fun AppScreenPrimarySimulatorIndicator(
    simulatorId: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSimulatorSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box {
        AppScreenPrimarySimulatorIcon(simulatorId = simulatorId, modifier = modifier)
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
        ) {
            Simulator.entries.forEach { simulator ->
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
                        onSimulatorSelected(simulator.id)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}
